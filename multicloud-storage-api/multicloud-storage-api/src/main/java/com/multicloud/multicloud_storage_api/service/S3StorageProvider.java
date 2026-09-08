package com.multicloud.multicloud_storage_api.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;

@Component
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3StorageProvider(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    @Override
    public String upload(byte[] data, String filename) throws IOException {

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(data)
        );

        return filename;
    }

    @Override
    public Resource download(String storedFilename) throws IOException {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storedFilename)
                .build();

        return new InputStreamResource(
                s3Client.getObject(request)
        );
    }

    @Override
    public void delete(String storedFilename) throws IOException {

        var request = software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(storedFilename)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public boolean exists(String storedFilename) {

        try {
            var request = software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .build();

            s3Client.headObject(request);

            return true;

        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            return e.statusCode() != 404;
        }
    }
}