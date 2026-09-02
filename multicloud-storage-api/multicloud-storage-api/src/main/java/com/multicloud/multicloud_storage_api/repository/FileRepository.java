package com.multicloud.multicloud_storage_api.repository;

import com.multicloud.multicloud_storage_api.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByUserEmail(String userEmail);

    Optional<FileMetadata> findByIdAndUserEmail(Long id, String userEmail);
}