package com.multicloud.multicloud_storage_api.controller;

import com.multicloud.multicloud_storage_api.entity.FileMetadata;
import com.multicloud.multicloud_storage_api.repository.FileRepository;
import com.multicloud.multicloud_storage_api.service.StorageProvider;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileRepository fileRepository;
    private final StorageProvider storageProvider;

    public FileController(
            FileRepository fileRepository,
            StorageProvider storageProvider
    ) {
        this.fileRepository = fileRepository;
        this.storageProvider = storageProvider;
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

            String storedFilename =
                    storageProvider.upload(file);

            FileMetadata metadata =
                    new FileMetadata();

            metadata.setOriginalFilename(
                    file.getOriginalFilename()
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

            return ResponseEntity.internalServerError()
                    .body("File upload failed");
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

            Resource resource =
                    storageProvider.download(
                            file.getStoredFilename()
                    );

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

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

        try {

            storageProvider.delete(
                    file.getStoredFilename()
            );

            fileRepository.delete(file);

            return ResponseEntity.ok(
                    "File deleted successfully"
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("File deletion failed");
        }
    }
}