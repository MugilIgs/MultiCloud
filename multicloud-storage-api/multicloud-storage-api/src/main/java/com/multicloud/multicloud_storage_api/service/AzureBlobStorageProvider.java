package com.multicloud.multicloud_storage_api.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class AzureBlobStorageProvider implements StorageProvider {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageProvider(
            @Value("${azure.storage.account-name}") String accountName,
            @Value("${azure.storage.container-name}") String containerName
    ) {

        String connectionString =
                System.getenv("AZURE_STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                    "AZURE_STORAGE_CONNECTION_STRING is not configured"
            );
        }

        BlobServiceClient blobServiceClient =
                new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

        this.containerClient =
                blobServiceClient.getBlobContainerClient(containerName);
    }

    @Override
    public String upload(byte[] data, String filename) throws IOException {

        BlobClient blobClient =
                containerClient.getBlobClient(filename);

        blobClient.upload(
                new ByteArrayInputStream(data),
                data.length,
                true
        );

        return filename;
    }

    @Override
    public Resource download(String storedFilename) throws IOException {

        BlobClient blobClient =
                containerClient.getBlobClient(storedFilename);

        return new InputStreamResource(
                blobClient.openInputStream()
        );
    }

    @Override
    public void delete(String storedFilename) {

        BlobClient blobClient =
                containerClient.getBlobClient(storedFilename);

        blobClient.delete();
    }

    @Override
    public boolean exists(String storedFilename) {

        BlobClient blobClient =
                containerClient.getBlobClient(storedFilename);

        return blobClient.exists();
    }
}