package com.multicloud.multicloud_storage_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import com.multicloud.multicloud_storage_api.service.StorageProviderType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
@Entity
@Table(name = "files")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFilename;

    private String storedFilename;

    private String contentType;

    private Long size;

    private String storagePath;
    @Enumerated(EnumType.STRING)
    private StorageProviderType primaryProvider;

    @Enumerated(EnumType.STRING)
    private StorageProviderType replicaProvider;

    private String userEmail;

    private LocalDateTime uploadedAt;

    public FileMetadata() {
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public StorageProviderType getPrimaryProvider() {
        return primaryProvider;
    }

    public void setPrimaryProvider(StorageProviderType primaryProvider) {
        this.primaryProvider = primaryProvider;
    }

    public StorageProviderType getReplicaProvider() {
        return replicaProvider;
    }

    public void setReplicaProvider(StorageProviderType replicaProvider) {
        this.replicaProvider = replicaProvider;
    }
}