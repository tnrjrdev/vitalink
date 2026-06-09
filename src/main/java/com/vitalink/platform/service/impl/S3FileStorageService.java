package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.exception.StorageException;
import com.vitalink.platform.config.AwsProperties;
import com.vitalink.platform.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "true")
public class S3FileStorageService implements FileStorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties properties;

    public S3FileStorageService(S3Client s3Client, S3Presigner s3Presigner, AwsProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public void upload(String key, byte[] content, String contentType) {
        String bucket = properties.getS3().getBucket();
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.info("Arquivo enviado ao S3: bucket={}, key={}, bytes={}", bucket, key, content.length);
        } catch (Exception ex) {
            throw new StorageException("Falha ao enviar arquivo ao S3 (key=" + key + ")", ex);
        }
    }

    @Override
    public URL generatePresignedDownloadUrl(String key) {
        String bucket = properties.getS3().getBucket();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(properties.getS3().getPresignedUrlExpirationMinutes()))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url();
        } catch (Exception ex) {
            throw new StorageException("Falha ao gerar URL pre-assinada do S3 (key=" + key + ")", ex);
        }
    }

    @Override
    public void delete(String key) {
        String bucket = properties.getS3().getBucket();
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            log.info("Arquivo removido do S3: bucket={}, key={}", bucket, key);
        } catch (Exception ex) {
            throw new StorageException("Falha ao remover arquivo do S3 (key=" + key + ")", ex);
        }
    }
}
