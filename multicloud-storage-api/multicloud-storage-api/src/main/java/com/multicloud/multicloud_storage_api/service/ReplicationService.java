package com.multicloud.multicloud_storage_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReplicationService {

    private final StorageProviderSelector storageProviderSelector;
    private final StorageProviderType replicationProviderType;

    public ReplicationService(
            StorageProviderSelector storageProviderSelector,
            @Value("${replication.provider}") String replicationProvider
    ) {
        this.storageProviderSelector = storageProviderSelector;

        this.replicationProviderType =
                StorageProviderType.valueOf(
                        replicationProvider.toUpperCase()
                );
    }

    public String replicate(
            byte[] encryptedData,
            String filename,
            StorageProviderType primaryProvider
    ) throws Exception {

        if (replicationProviderType == primaryProvider) {
            throw new IllegalStateException(
                    "Replication provider must be different from primary provider"
            );
        }

        StorageProvider provider =
                storageProviderSelector.getProvider(
                        replicationProviderType
                );

        return provider.upload(
                encryptedData,
                filename
        );
    }
    public void deleteReplica(String filename) throws Exception {

        StorageProvider provider =
                storageProviderSelector.getProvider(
                        replicationProviderType
                );

        provider.delete(filename);
    }
    public StorageProviderType getReplicationProviderType() {
        return replicationProviderType;
    }
}