package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class LocalStorageProvider implements StorageProvider {

    private final Path uploadDirectory = Paths.get("uploads");

    @Override
    public String upload(MultipartFile file) throws IOException {

        Files.createDirectories(uploadDirectory);

        String storedFilename =
                java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path destination = uploadDirectory.resolve(storedFilename);

        file.transferTo(destination);

        return storedFilename;
    }

    @Override
    public Resource download(String storedFilename) throws IOException {

        Path filePath = uploadDirectory.resolve(storedFilename);

        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new IOException("File not found: " + storedFilename);
        }

        return resource;
    }

    @Override
    public void delete(String storedFilename) throws IOException {

        Path filePath = uploadDirectory.resolve(storedFilename);

        Files.deleteIfExists(filePath);
    }

    @Override
    public boolean exists(String storedFilename) {

        return Files.exists(uploadDirectory.resolve(storedFilename));
    }
}