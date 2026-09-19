package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class CloudScoringService {

    /*
     * Adaptive cloud scoring model.
     *
     * Score components:
     *
     * 50% - Current health
     * 30% - Current latency
     * 20% - Learned historical performance
     *
     * The historical performance value is updated
     * continuously using an exponential moving average.
     */

    private final Map<StorageProviderType, Double> historicalPerformance =
            new EnumMap<>(StorageProviderType.class);

    public CloudScoringService() {

        /*
         * Initial neutral performance values.
         *
         * These values are not fixed reliability rankings.
         * They act only as starting points before the system
         * collects actual runtime observations.
         */

        historicalPerformance.put(
                StorageProviderType.AWS_S3,
                90.0
        );

        historicalPerformance.put(
                StorageProviderType.GCP,
                90.0
        );

        historicalPerformance.put(
                StorageProviderType.AZURE,
                90.0
        );

        historicalPerformance.put(
                StorageProviderType.LOCAL,
                90.0
        );
    }

    public synchronized double calculateScore(
            StorageProviderType providerType,
            Map<String, Object> health
    ) {

        boolean healthy =
                Boolean.TRUE.equals(
                        health.get("healthy")
                );

        /*
         * An unhealthy provider receives zero score.
         */
        if (!healthy) {

            updateHistoricalPerformance(
                    providerType,
                    0.0
            );

            return 0.0;
        }

        Number latencyValue =
                (Number) health.get("latencyMs");

        double latency =
                latencyValue.doubleValue();

        /*
         * Convert latency into a score.
         *
         * Lower latency = higher score.
         *
         * 1000 ms is treated as the maximum
         * acceptable latency.
         */
        double latencyScore =
                Math.max(
                        0.0,
                        100.0 -
                                (latency / 1000.0 * 100.0)
                );

        /*
         * Create the current performance observation.
         *
         * Health contributes 100 when healthy.
         * Latency contributes directly to the
         * current performance observation.
         */
        double currentPerformance =
                (100.0 * 0.50) +
                        (latencyScore * 0.50);

        /*
         * Update historical performance using
         * an exponential moving average.
         *
         * 70% previous experience
         * 30% latest observation
         */
        updateHistoricalPerformance(
                providerType,
                currentPerformance
        );

        double learnedPerformance =
                historicalPerformance.getOrDefault(
                        providerType,
                        90.0
                );

        /*
         * Final adaptive score.
         *
         * 50% health
         * 30% current latency
         * 20% learned historical performance
         */
        return
                (100.0 * 0.50) +
                        (latencyScore * 0.30) +
                        (learnedPerformance * 0.20);
    }

    /*
     * Exponential Moving Average (EMA)
     *
     * This allows the system to gradually learn from
     * new cloud-performance observations while still
     * retaining historical information.
     */
    private void updateHistoricalPerformance(
            StorageProviderType providerType,
            double currentPerformance
    ) {

        double previousPerformance =
                historicalPerformance.getOrDefault(
                        providerType,
                        90.0
                );

        double learningRate = 0.30;

        double updatedPerformance =
                (previousPerformance * (1.0 - learningRate)) +
                        (currentPerformance * learningRate);

        historicalPerformance.put(
                providerType,
                updatedPerformance
        );
    }

    /*
     * Allows the dashboard/debugging layer to inspect
     * the learned performance of each provider.
     */
    public synchronized Map<String, Double> getHistoricalPerformance() {

        Map<String, Double> result = new java.util.LinkedHashMap<>();

        for (Map.Entry<StorageProviderType, Double> entry :
                historicalPerformance.entrySet()) {

            result.put(
                    entry.getKey().name(),
                    entry.getValue()
            );
        }

        return result;
    }
}