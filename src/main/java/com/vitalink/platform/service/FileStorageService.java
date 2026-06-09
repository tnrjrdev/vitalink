package com.vitalink.platform.service;

import java.net.URL;

/**
 * Porta de armazenamento de arquivos. A implementacao concreta depende do
 * profile/configuracao: S3 (app.aws.enabled=true) ou armazenamento local
 * (fallback para desenvolvimento sem AWS).
 */
public interface FileStorageService {
    void upload(String key, byte[] content, String contentType);

    URL generatePresignedDownloadUrl(String key);

    void delete(String key);
}
