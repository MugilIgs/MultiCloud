package com.multicloud.multicloud_storage_api.controller;

import com.multicloud.multicloud_storage_api.entity.FileMetadata;
import com.multicloud.multicloud_storage_api.repository.FileRepository;
import com.multicloud.multicloud_storage_api.service.StorageProvider;
import com.multicloud.multicloud_storage_api.service.StorageProviderSelector;
import com.multicloud.multicloud_storage_api.service.ReplicationService;
import com.multicloud.multicloud_storage_api.service.StorageProviderType;
import com.multicloud.multicloud_storage_api.service.AdaptiveStorageProviderService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.multicloud.multicloud_storage_api.service.EncryptionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileRepository fileRepository;
    private final StorageProvider storageProvider;
    private final EncryptionService encryptionService;
    private final ReplicationService replicationService;
    private final AdaptiveStorageProviderService adaptiveStorageProviderService;
    private final StorageProviderSelector storageProviderSelector;

    public FileController(
            FileRepository fileRepository,
            StorageProviderSelector storageProviderSelector,
            EncryptionService encryptionService,
            ReplicationService replicationService,
            AdaptiveStorageProviderService adaptiveStorageProviderService
    ) {
        this.fileRepository = fileRepository;
        this.storageProvider = storageProviderSelector.getProvider();
        this.encryptionService = encryptionService;
        this.replicationService = replicationService;
        this.adaptiveStorageProviderService = adaptiveStorageProviderService;
        this.storageProviderSelector = storageProviderSelector;
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        try {

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("File cannot be empty");
            }

            String userEmail =
                    authentication.getName();

            byte[] encryptedData =
                    encryptionService.encrypt(file.getInputStream());

            StorageProviderType primaryProviderType =
                    adaptiveStorageProviderService.selectBestProvider();

            StorageProvider selectedProvider =
                    storageProviderSelector.getProvider(
                            primaryProviderType
                    );

            String storedFilename =
                    selectedProvider.upload(
                            encryptedData,
                            file.getOriginalFilename()
                    );
            String replicaStoredFilename =
                    replicationService.replicate(
                            encryptedData,
                            file.getOriginalFilename(),
                            primaryProviderType
                    );
            FileMetadata metadata =
                    new FileMetadata();

            metadata.setOriginalFilename(
                    file.getOriginalFilename()
            );
            metadata.setPrimaryProvider(
                    primaryProviderType
            );

            metadata.setReplicaProvider(
                    replicationService.getReplicationProviderType()
            );
            metadata.setStoredFilename(
                    storedFilename
            );

            metadata.setContentType(
                    file.getContentType()
            );

            metadata.setSize(
                    file.getSize()
            );

            metadata.setStoragePath(
                    "uploads/" + storedFilename
            );

            metadata.setUserEmail(
                    userEmail
            );

            metadata.setUploadedAt(
                    LocalDateTime.now()
            );

            FileMetadata savedFile =
                    fileRepository.save(metadata);

            return ResponseEntity.ok(savedFile);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getUserFiles(
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        List<FileMetadata> files =
                fileRepository.findByUserEmail(userEmail);

        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        Optional<FileMetadata> optionalFile =
                fileRepository.findByIdAndUserEmail(
                        id,
                        userEmail
                );

        if (optionalFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FileMetadata file =
                optionalFile.get();

        try {

            StorageProvider downloadProvider =
                    storageProviderSelector.getProvider(
                            file.getPrimaryProvider()
                    );

            Resource encryptedResource;

            try {

                encryptedResource =
                        downloadProvider.download(
                                file.getStoredFilename()
                        );

                if (!encryptedResource.exists()) {
                    throw new RuntimeException(
                            "Primary provider file not found"
                    );
                }

            } catch (Exception primaryException) {

                if (file.getReplicaProvider() == null) {
                    return ResponseEntity.internalServerError()
                            .body("Primary storage unavailable and no replica exists");
                }

                StorageProvider replicaProvider =
                        storageProviderSelector.getProvider(
                                file.getReplicaProvider()
                        );

                try {

                    encryptedResource =
                            replicaProvider.download(
                                    file.getStoredFilename()
                            );

                    if (!encryptedResource.exists()) {
                        return ResponseEntity.notFound().build();
                    }

                } catch (Exception replicaException) {

                    return ResponseEntity.internalServerError()
                            .body("File unavailable from both primary and replica");
                }
            }

            byte[] decryptedData =
                    encryptionService.decrypt(
                            encryptedResource.getInputStream()
                    );

            Resource resource =
                    new org.springframework.core.io.ByteArrayResource(
                            decryptedData
                    );
            String contentType =
                    file.getContentType();

            if (contentType == null) {
                contentType =
                        "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                                    file.getOriginalFilename() +
                                    "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("File download failed");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String userEmail =
                authentication.getName();

        Optional<FileMetadata> optionalFile =
                fileRepository.findByIdAndUserEmail(
                        id,
                        userEmail
                );

        if (optionalFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FileMetadata file =
                optionalFile.get();

        boolean primaryDeleted = false;
        boolean replicaDeleted = false;

        try {

            // Delete primary copy
            try {

                StorageProvider primaryProvider =
                        storageProviderSelector.getProvider(
                                file.getPrimaryProvider()
                        );

                primaryProvider.delete(
                        file.getStoredFilename()
                );

                primaryDeleted = true;

            } catch (Exception e) {

                // Primary may already be unavailable.
                // Continue and attempt replica deletion.
                System.out.println(
                        "Primary deletion skipped/failed: "
                                + e.getMessage()
                );
            }

            // Delete replica copy
            try {

                replicationService.deleteReplica(
                        file.getStoredFilename()
                );

                replicaDeleted = true;

            } catch (Exception e) {

                System.out.println(
                        "Replica deletion failed: "
                                + e.getMessage()
                );
            }

            // Remove database metadata only when both
            // storage copies have been successfully handled.
            if (primaryDeleted || replicaDeleted) {

                fileRepository.delete(file);

                return ResponseEntity.ok(
                        "File deleted successfully"
                );
            }

            return ResponseEntity.internalServerError()
                    .body(
                            "File deletion failed: " +
                                    "both primary and replica are unavailable"
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("File deletion failed");
        }
    }
}