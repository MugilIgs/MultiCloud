package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;

@Service
public class ReplicationService {

    private final StorageProviderSelector storageProviderSelector;

    public ReplicationService(
            StorageProviderSelector storageProviderSelector
    ) {
        this.storageProviderSelector = storageProviderSelector;
    }

    public String replicate(
            byte[] encryptedData,
            String filename,
            StorageProviderType providerType
    ) throws Exception {

        StorageProvider provider =
                storageProviderSelector.getProvider(providerType);

        return provider.upload(
                encryptedData,
                filename
        );
    }
}