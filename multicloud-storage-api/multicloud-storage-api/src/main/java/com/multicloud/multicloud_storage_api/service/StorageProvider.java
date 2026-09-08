package com.multicloud.multicloud_storage_api.service;

import org.springframework.core.io.Resource;

import java.io.IOException;

public interface StorageProvider {

    String upload(
            byte[] data,
            String filename
    ) throws IOException;

    Resource download(
            String storedFilename
    ) throws IOException;

    void delete(
            String storedFilename
    ) throws IOException;

    boolean exists(
            String storedFilename
    );
}