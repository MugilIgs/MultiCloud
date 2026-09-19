package com.multicloud.multicloud_storage_api.controller;

import com.multicloud.multicloud_storage_api.service.CloudHealthService;
import com.multicloud.multicloud_storage_api.service.StorageProviderType;
import com.multicloud.multicloud_storage_api.service.AdaptiveStorageProviderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class CloudHealthController {

    private final CloudHealthService cloudHealthService;
    private final AdaptiveStorageProviderService adaptiveStorageProviderService;
    public CloudHealthController(
            CloudHealthService cloudHealthService,
            AdaptiveStorageProviderService adaptiveStorageProviderService
    ) {
        this.cloudHealthService = cloudHealthService;
        this.adaptiveStorageProviderService = adaptiveStorageProviderService;
    }
    @GetMapping("/aws")
    public Map<String, Object> checkAws() {

        return cloudHealthService.checkProvider(
                StorageProviderType.AWS_S3
        );
    }

    @GetMapping("/gcp")
    public Map<String, Object> checkGcp() {

        return cloudHealthService.checkProvider(
                StorageProviderType.GCP
        );
    }

    @GetMapping("/azure")
    public Map<String, Object> checkAzure() {

        return cloudHealthService.checkProvider(
                StorageProviderType.AZURE
        );
    }

    @GetMapping("/local")
    public Map<String, Object> checkLocal() {

        return cloudHealthService.checkProvider(
                StorageProviderType.LOCAL
        );
    }

    @GetMapping("/status")
    public Map<String, Map<String, Object>> checkAllProviders() {

        Map<String, Map<String, Object>> result =
                new java.util.LinkedHashMap<>();

        result.put("AWS_S3",
                cloudHealthService.checkProvider(
                        StorageProviderType.AWS_S3
                ));

        result.put("GCP",
                cloudHealthService.checkProvider(
                        StorageProviderType.GCP
                ));

        result.put("AZURE",
                cloudHealthService.checkProvider(
                        StorageProviderType.AZURE
                ));

        result.put("LOCAL",
                cloudHealthService.checkProvider(
                        StorageProviderType.LOCAL
                ));

        return result;
    }
    @GetMapping("/best")
    public Map<String, Object> getBestProvider() {

        StorageProviderType bestProvider =
                adaptiveStorageProviderService.selectBestProvider();

        Map<String, Object> result =
                new java.util.LinkedHashMap<>();

        result.put("selectedProvider", bestProvider.name());

        return result;
    }
    @GetMapping("/scores")
    public Map<String, Object> getProviderScores() {

        return adaptiveStorageProviderService.getProviderScores();
    }
    @GetMapping("/historical")
    public Map<String, Double> getHistoricalPerformance() {

        return adaptiveStorageProviderService
                .getCloudScoringService()
                .getHistoricalPerformance();
    }
    @GetMapping("/analysis")
    public Map<String, Object> getAdaptiveAnalysis() {
        return adaptiveStorageProviderService.analyzeProviders();
    }
}