package com.multicloud.multicloud_storage_api;

import com.multicloud.multicloud_storage_api.service.CloudScoringService;
import com.multicloud.multicloud_storage_api.service.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CloudScoringServiceTest {

    @Test
    void healthyProviderShouldReceivePositiveScore() {

        CloudScoringService scoringService =
                new CloudScoringService();

        Map<String, Object> health =
                Map.of(
                        "healthy", true,
                        "latencyMs", 200
                );

        double score =
                scoringService.calculateScore(
                        StorageProviderType.AWS_S3,
                        health
                );

        assertTrue(score > 0);
    }
    @Test
    void unhealthyProviderShouldReceiveZeroScore() {

        CloudScoringService scoringService =
                new CloudScoringService();

        Map<String, Object> health =
                Map.of(
                        "healthy", false,
                        "latencyMs", 100
                );

        double score =
                scoringService.calculateScore(
                        StorageProviderType.GCP,
                        health
                );

        assertTrue(score == 0.0);
    }
    @Test
    void lowerLatencyProviderShouldReceiveHigherScore() {

        CloudScoringService scoringService =
                new CloudScoringService();

        Map<String, Object> fastHealth =
                Map.of(
                        "healthy", true,
                        "latencyMs", 100
                );

        Map<String, Object> slowHealth =
                Map.of(
                        "healthy", true,
                        "latencyMs", 800
                );

        double fastScore =
                scoringService.calculateScore(
                        StorageProviderType.GCP,
                        fastHealth
                );

        double slowScore =
                scoringService.calculateScore(
                        StorageProviderType.GCP,
                        slowHealth
                );

        assertTrue(fastScore > slowScore);
    }
    @Test
    void unhealthyProviderShouldScoreLowerThanHealthyProvider() {

        CloudScoringService scoringService =
                new CloudScoringService();

        Map<String, Object> healthy =
                Map.of(
                        "healthy", true,
                        "latencyMs", 200
                );

        Map<String, Object> unhealthy =
                Map.of(
                        "healthy", false,
                        "latencyMs", 200
                );

        double healthyScore =
                scoringService.calculateScore(
                        StorageProviderType.AWS_S3,
                        healthy
                );

        double unhealthyScore =
                scoringService.calculateScore(
                        StorageProviderType.AWS_S3,
                        unhealthy
                );

        assertTrue(
                healthyScore > unhealthyScore
        );
    }
    @Test
    void veryHighLatencyShouldNotProduceNegativeScore() {

        CloudScoringService scoringService =
                new CloudScoringService();

        Map<String, Object> health =
                Map.of(
                        "healthy", true,
                        "latencyMs", 5000
                );

        double score =
                scoringService.calculateScore(
                        StorageProviderType.AWS_S3,
                        health
                );

        assertTrue(score >= 0.0);
    }
}