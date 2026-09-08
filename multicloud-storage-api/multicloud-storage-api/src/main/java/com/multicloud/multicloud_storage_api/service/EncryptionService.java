package com.multicloud.multicloud_storage_api.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Base64;

    @Service
    public class EncryptionService {

        private static final String ALGORITHM = "AES/GCM/NoPadding";
        private static final int IV_LENGTH = 12;
        private static final int TAG_LENGTH = 128;

        private final SecretKeySpec secretKey;
        private final SecureRandom secureRandom;

        public EncryptionService() {

            String encodedKey =
                    System.getenv("CLOUD_ENCRYPTION_KEY");

            if (encodedKey == null || encodedKey.isBlank()) {
                throw new IllegalStateException(
                        "CLOUD_ENCRYPTION_KEY is not configured"
                );
            }

            byte[] keyBytes =
                    Base64.getDecoder().decode(encodedKey);

            if (keyBytes.length != 32) {
                throw new IllegalStateException(
                        "CLOUD_ENCRYPTION_KEY must contain exactly 32 bytes"
                );
            }

            this.secretKey =
                    new SecretKeySpec(keyBytes, "AES");

            this.secureRandom =
                    new SecureRandom();
        }

        public byte[] encrypt(InputStream inputStream)
                throws IOException {

            try {

                byte[] plaintext =
                        inputStream.readAllBytes();

                byte[] iv =
                        new byte[IV_LENGTH];

                secureRandom.nextBytes(iv);

                Cipher cipher =
                        Cipher.getInstance(ALGORITHM);

                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        secretKey,
                        new GCMParameterSpec(TAG_LENGTH, iv)
                );

                byte[] encrypted =
                        cipher.doFinal(plaintext);

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();

                // Store IV at the beginning of the encrypted data
                output.write(iv);
                output.write(encrypted);

                return output.toByteArray();

            } catch (Exception e) {

                throw new IOException(
                        "File encryption failed",
                        e
                );
            }
        }

        public byte[] decrypt(InputStream inputStream)
                throws IOException {

            try {

                byte[] encryptedData =
                        inputStream.readAllBytes();

                if (encryptedData.length <= IV_LENGTH) {
                    throw new IOException(
                            "Invalid encrypted file"
                    );
                }

                byte[] iv =
                        new byte[IV_LENGTH];

                System.arraycopy(
                        encryptedData,
                        0,
                        iv,
                        0,
                        IV_LENGTH
                );

                byte[] ciphertext =
                        new byte[
                                encryptedData.length - IV_LENGTH
                                ];

                System.arraycopy(
                        encryptedData,
                        IV_LENGTH,
                        ciphertext,
                        0,
                        ciphertext.length
                );

                Cipher cipher =
                        Cipher.getInstance(ALGORITHM);

                cipher.init(
                        Cipher.DECRYPT_MODE,
                        secretKey,
                        new GCMParameterSpec(TAG_LENGTH, iv)
                );

                return cipher.doFinal(ciphertext);

            } catch (Exception e) {

                throw new IOException(
                        "File decryption failed",
                        e
                );
            }
        }
    }
