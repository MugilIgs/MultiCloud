package com.multicloud.multicloud_storage_api.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.core.io.Resource;

public interface StorageProvider {

    String upload(MultipartFile file) throws IOException;

    Resource download(String storedFilename) throws IOException;

    void delete(String storedFilename) throws IOException;

    boolean exists(String storedFilename);
}
