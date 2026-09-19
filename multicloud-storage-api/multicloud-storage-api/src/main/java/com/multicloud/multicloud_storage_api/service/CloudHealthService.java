package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudHealthService {

    private final StorageProviderSelector storageProviderSelector;

    public CloudHealthService(
            StorageProviderSelector storageProviderSelector
    ) {
        this.storageProviderSelector = storageProviderSelector;
    }

    public Map<String, Object> checkProvider(
            StorageProviderType providerType
    ) {

        long startTime = System.currentTimeMillis();

        String healthCheckFilename =
                "__health_check_" + UUID.randomUUID() + ".txt";

        try {

            StorageProvider provider =
                    storageProviderSelector.getProvider(providerType);

            byte[] healthCheckData =
                    "health-check".getBytes(StandardCharsets.UTF_8);

            provider.upload(
                    healthCheckData,
                    healthCheckFilename
            );

            boolean exists =
                    provider.exists(healthCheckFilename);

            provider.delete(healthCheckFilename);

            long latency =
                    System.currentTimeMillis() - startTime;

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("provider", providerType.name());
            result.put("healthy", exists);
            result.put("latencyMs", latency);

            return result;

        } catch (Exception e) {

            long latency =
                    System.currentTimeMillis() - startTime;

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("provider", providerType.name());
            result.put("healthy", false);
            result.put("latencyMs", latency);

            return result;
        }
    }
}