package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    private final StorageProvider storageProvider;

    public FileStorageService(StorageProviderSelector providerSelector) {
        this.storageProvider = providerSelector.getProvider();
    }

    public String storeFile(MultipartFile file) throws IOException {
        return storageProvider.upload(file.getBytes(), file.getOriginalFilename());
    }

    public void deleteFile(String storedFilename) throws IOException {
        storageProvider.delete(storedFilename);
    }

    public boolean fileExists(String storedFilename) {
        return storageProvider.exists(storedFilename);
    }
}