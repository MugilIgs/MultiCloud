package com.multicloud.multicloud_storage_api;

import com.multicloud.multicloud_storage_api.service.EncryptionService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncryptionServiceTest {

    @Test
    void encryptThenDecryptShouldReturnOriginalData() throws Exception {

        EncryptionService encryptionService =
                new EncryptionService();

        String originalText =
                "Cloud of Clouds encryption test";

        byte[] originalData =
                originalText.getBytes(StandardCharsets.UTF_8);

        byte[] encryptedData =
                encryptionService.encrypt(
                        new ByteArrayInputStream(originalData)
                );

        byte[] decryptedData =
                encryptionService.decrypt(
                        new ByteArrayInputStream(encryptedData)
                );

        String decryptedText =
                new String(
                        decryptedData,
                        StandardCharsets.UTF_8
                );

        assertEquals(
                originalText,
                decryptedText
        );
    }
    @Test
    void encryptedDataShouldNotMatchOriginalData() throws Exception {

        EncryptionService encryptionService =
                new EncryptionService();

        String originalText =
                "Cloud of Clouds secret data";

        byte[] originalData =
                originalText.getBytes(StandardCharsets.UTF_8);

        byte[] encryptedData =
                encryptionService.encrypt(
                        new ByteArrayInputStream(originalData)
                );

        org.junit.jupiter.api.Assertions.assertFalse(
                java.util.Arrays.equals(
                        originalData,
                        encryptedData
                )
        );
    }
}