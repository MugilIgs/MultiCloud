package com.multicloud.multicloud_storage_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageProviderSelector {

    private final StorageProvider storageProvider;

    private final LocalStorageProvider localStorageProvider;
    private final S3StorageProvider s3StorageProvider;
    private final GcsStorageProvider gcsStorageProvider;
    private final AzureBlobStorageProvider azureBlobStorageProvider;

    public StorageProviderSelector(
            LocalStorageProvider localStorageProvider,
            S3StorageProvider s3StorageProvider,
            GcsStorageProvider gcsStorageProvider,
            AzureBlobStorageProvider azureBlobStorageProvider,
            @Value("${storage.provider}") String provider
    ) {

        this.localStorageProvider = localStorageProvider;
        this.s3StorageProvider = s3StorageProvider;
        this.gcsStorageProvider = gcsStorageProvider;
        this.azureBlobStorageProvider = azureBlobStorageProvider;

        StorageProviderType providerType =
                StorageProviderType.valueOf(
                        provider.toUpperCase()
                );

        this.storageProvider =
                getProvider(providerType);
    }

    public StorageProvider getProvider() {
        return storageProvider;
    }

    public StorageProvider getProvider(StorageProviderType providerType) {

        return switch (providerType) {

            case LOCAL ->
                    localStorageProvider;

            case AWS_S3 ->
                    s3StorageProvider;

            case GCP ->
                    gcsStorageProvider;

            case AZURE ->
                    azureBlobStorageProvider;
        };
    }
}