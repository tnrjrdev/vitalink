package com.vitalink.platform.service;

import com.vitalink.platform.common.exception.StorageException;
import com.vitalink.platform.config.AwsProperties;
import com.vitalink.platform.service.impl.S3FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3FileStorageService")
class S3FileStorageServiceTest {
    @Mock private S3Client s3Client;
    @Mock private S3Presigner s3Presigner;

    private S3FileStorageService service;

    @BeforeEach
    void setUp() {
        AwsProperties properties = new AwsProperties();
        properties.getS3().setBucket("vitalink-test");
        service = new S3FileStorageService(s3Client, s3Presigner, properties);
    }

    @Test
    @DisplayName("envia o objeto para o bucket configurado")
    void shouldUpload() {
        service.upload("k/exame.pdf", new byte[]{1, 2, 3}, "application/pdf");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("encapsula falha do S3 em StorageException no upload")
    void shouldWrapUploadFailure() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("s3 down"));

        assertThatThrownBy(() -> service.upload("k", new byte[]{1}, "text/plain"))
                .isInstanceOf(StorageException.class);
    }

    @Test
    @DisplayName("remove o objeto do bucket")
    void shouldDelete() {
        service.delete("k/exame.pdf");
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("encapsula falha do presigner em StorageException")
    void shouldWrapPresignFailure() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("presign down"));

        assertThatThrownBy(() -> service.generatePresignedDownloadUrl("k"))
                .isInstanceOf(StorageException.class);
    }
}
