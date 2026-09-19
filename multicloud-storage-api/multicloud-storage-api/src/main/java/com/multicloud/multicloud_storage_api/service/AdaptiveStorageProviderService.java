package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdaptiveStorageProviderService {

    private final CloudHealthService cloudHealthService;
    private final CloudScoringService cloudScoringService;
    private final StorageProviderSelector storageProviderSelector;
    private final StorageProviderType replicationProviderType;

    public AdaptiveStorageProviderService(
            CloudHealthService cloudHealthService,
            CloudScoringService cloudScoringService,
            StorageProviderSelector storageProviderSelector,
            @Value("${replication.provider}") String replicationProvider
    ) {

        this.cloudHealthService = cloudHealthService;
        this.cloudScoringService = cloudScoringService;
        this.storageProviderSelector = storageProviderSelector;

        this.replicationProviderType =
                StorageProviderType.valueOf(
                        replicationProvider.toUpperCase()
                );
    }

    public StorageProviderType selectBestProvider() {

        StorageProviderType[] providers = {
                StorageProviderType.AWS_S3,
                StorageProviderType.GCP,
                StorageProviderType.AZURE
        };

        StorageProviderType bestProvider = null;
        double bestScore = -1.0;

        for (StorageProviderType providerType : providers) {

            if (providerType == replicationProviderType) {
                continue;
            }

            Map<String, Object> health =
                    cloudHealthService.checkProvider(providerType);

            boolean healthy =
                    Boolean.TRUE.equals(
                            health.get("healthy")
                    );

            if (!healthy) {
                continue;
            }

            double score =
                    cloudScoringService.calculateScore(
                            providerType,
                            health
                    );

            if (score > bestScore) {

                bestScore = score;
                bestProvider = providerType;
            }
        }

        if (bestProvider == null) {

            throw new IllegalStateException(
                    "No healthy cloud storage provider is available"
            );
        }

        return bestProvider;
    }

    public StorageProvider getBestProvider() {

        StorageProviderType providerType =
                selectBestProvider();

        return storageProviderSelector.getProvider(
                providerType
        );
    }

    public Map<String, Object> getProviderScores() {

        StorageProviderType[] providers = {
                StorageProviderType.AWS_S3,
                StorageProviderType.GCP,
                StorageProviderType.AZURE
        };

        Map<String, Object> scores =
                new LinkedHashMap<>();

        for (StorageProviderType providerType : providers) {

            if (providerType == replicationProviderType) {
                continue;
            }

            Map<String, Object> health =
                    cloudHealthService.checkProvider(providerType);

            double score =
                    cloudScoringService.calculateScore(
                            providerType,
                            health
                    );

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("healthy", health.get("healthy"));
            result.put("latencyMs", health.get("latencyMs"));
            result.put("score", score);

            scores.put(
                    providerType.name(),
                    result
            );
        }

        return scores;
    }
    public Map<String, Object> analyzeProviders() {

        StorageProviderType[] providers = {
                StorageProviderType.AWS_S3,
                StorageProviderType.GCP,
                StorageProviderType.AZURE
        };

        Map<String, Object> analysis =
                new LinkedHashMap<>();

        StorageProviderType bestProvider = null;
        double bestScore = -1.0;

        for (StorageProviderType providerType : providers) {

            Map<String, Object> health =
                    cloudHealthService.checkProvider(providerType);

            double score =
                    cloudScoringService.calculateScore(
                            providerType,
                            health
                    );

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("healthy", health.get("healthy"));
            result.put("latencyMs", health.get("latencyMs"));
            result.put("score", score);
            result.put(
                    "role",
                    providerType == replicationProviderType
                            ? "REPLICA"
                            : "CANDIDATE"
            );

            analysis.put(
                    providerType.name(),
                    result
            );

            boolean healthy =
                    Boolean.TRUE.equals(
                            health.get("healthy")
                    );

            if (
                    healthy &&
                            providerType != replicationProviderType &&
                            score > bestScore
            ) {
                bestScore = score;
                bestProvider = providerType;
            }
        }

        if (bestProvider == null) {
            throw new IllegalStateException(
                    "No healthy cloud storage provider is available"
            );
        }

        analysis.put(
                "selectedProvider",
                bestProvider.name()
        );

        return analysis;
    }
    public CloudScoringService getCloudScoringService() {
        return cloudScoringService;
    }
}