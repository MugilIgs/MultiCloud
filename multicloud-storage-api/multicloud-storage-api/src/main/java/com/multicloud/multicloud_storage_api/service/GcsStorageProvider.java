package com.multicloud.multicloud_storage_api.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class GcsStorageProvider implements StorageProvider {

    private final Storage storage;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    public GcsStorageProvider() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    @Override
    public String upload(byte[] data, String filename) throws IOException {

        BlobId blobId =
                BlobId.of(bucketName, filename);

        BlobInfo blobInfo =
                BlobInfo.newBuilder(blobId)
                        .build();

        storage.create(
                blobInfo,
                data
        );

        return filename;
    }

    @Override
    public Resource download(String storedFilename) throws IOException {

        BlobId blobId = BlobId.of(bucketName, storedFilename);

        Blob blob = storage.get(blobId);

        if (blob == null) {
            throw new IOException("File not found in GCS: " + storedFilename);
        }

        byte[] content = blob.getContent();

        return new ByteArrayResource(content);
    }

    @Override
    public void delete(String storedFilename) throws IOException {

        BlobId blobId = BlobId.of(bucketName, storedFilename);

        boolean deleted = storage.delete(blobId);

        if (!deleted) {
            throw new IOException("File not found in GCS: " + storedFilename);
        }
    }

    @Override
    public boolean exists(String storedFilename) {

        BlobId blobId = BlobId.of(bucketName, storedFilename);

        return storage.get(blobId) != null;
    }
}