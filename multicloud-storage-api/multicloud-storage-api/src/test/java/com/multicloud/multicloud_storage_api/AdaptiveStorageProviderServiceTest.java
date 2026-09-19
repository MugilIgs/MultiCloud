package com.multicloud.multicloud_storage_api;

import com.multicloud.multicloud_storage_api.service.AdaptiveStorageProviderService;
import com.multicloud.multicloud_storage_api.service.CloudHealthService;
import com.multicloud.multicloud_storage_api.service.CloudScoringService;
import com.multicloud.multicloud_storage_api.service.StorageProviderSelector;
import com.multicloud.multicloud_storage_api.service.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
class AdaptiveStorageProviderServiceTest {

    @Test
    void shouldSelectAHealthyCloudProvider() {

        CloudHealthService cloudHealthService =
                org.mockito.Mockito.mock(
                        CloudHealthService.class
                );

        CloudScoringService cloudScoringService =
                new CloudScoringService();

        StorageProviderSelector storageProviderSelector =
                org.mockito.Mockito.mock(
                        StorageProviderSelector.class
                );

        org.mockito.Mockito.when(
                cloudHealthService.checkProvider(
                        StorageProviderType.GCP
                )
        ).thenReturn(
                Map.of(
                        "healthy", true,
                        "latencyMs", 100
                )
        );

        org.mockito.Mockito.when(
                cloudHealthService.checkProvider(
                        StorageProviderType.AZURE
                )
        ).thenReturn(
                Map.of(
                        "healthy", true,
                        "latencyMs", 800
                )
        );

        AdaptiveStorageProviderService service =
                new AdaptiveStorageProviderService(
                        cloudHealthService,
                        cloudScoringService,
                        storageProviderSelector,
                        "AWS_S3"
                );

        StorageProviderType selected =
                service.selectBestProvider();

        assertTrue(
                selected == StorageProviderType.GCP
        );
    }
    @Test
    void shouldIgnoreUnhealthyProvider() {

        CloudHealthService cloudHealthService =
                org.mockito.Mockito.mock(
                        CloudHealthService.class
                );

        CloudScoringService cloudScoringService =
                new CloudScoringService();

        StorageProviderSelector storageProviderSelector =
                org.mockito.Mockito.mock(
                        StorageProviderSelector.class
                );

        org.mockito.Mockito.when(
                cloudHealthService.checkProvider(
                        StorageProviderType.GCP
                )
        ).thenReturn(
                Map.of(
                        "healthy", false,
                        "latencyMs", 100
                )
        );

        org.mockito.Mockito.when(
                cloudHealthService.checkProvider(
                        StorageProviderType.AZURE
                )
        ).thenReturn(
                Map.of(
                        "healthy", true,
                        "latencyMs", 500
                )
        );

        AdaptiveStorageProviderService service =
                new AdaptiveStorageProviderService(
                        cloudHealthService,
                        cloudScoringService,
                        storageProviderSelector,
                        "AWS_S3"
                );

        StorageProviderType selected =
                service.selectBestProvider();

        assertTrue(
                selected == StorageProviderType.AZURE
        );
    }
}