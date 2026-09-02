package com.multicloud.multicloud_storage_api.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageProviderSelector {
    private final StorageProvider storageProvider;

    public StorageProviderSelector(
            LocalStorageProvider localStorageProvider,
            @Value("${storage.provider}") String provider
    ){
        StorageProviderType providerType = StorageProviderType.valueOf(provider.toUpperCase());

        switch(providerType){
            case LOCAL:
                this.storageProvider = localStorageProvider;
                break;
            default:
                throw new IllegalArgumentException("Storage provider not implemented yet: " + provider );
        }
    }
    public StorageProvider getProvider(){
        return storageProvider;
    }
}
