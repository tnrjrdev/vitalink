package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.exception.StorageException;
import com.vitalink.platform.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Fallback de armazenamento para desenvolvimento sem AWS. Persiste os arquivos
 * sob um diretorio temporario local e devolve uma URL file:// como "presigned".
 * Ativo quando app.aws.enabled e falso (default).
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {
    private final Path baseDir = Paths.get(System.getProperty("java.io.tmpdir"), "vitalink-storage");

    @PostConstruct
    void init() {
        log.warn("FileStorage LOCAL ativo (AWS desabilitada). Arquivos serao gravados em: {}", baseDir);
    }

    @Override
    public void upload(String key, byte[] content, String contentType) {
        try {
            Path target = resolve(key);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            log.info("Arquivo gravado localmente: {} ({} bytes)", target, content.length);
        } catch (Exception ex) {
            throw new StorageException("Falha ao gravar arquivo localmente (key=" + key + ")", ex);
        }
    }

    @Override
    public URL generatePresignedDownloadUrl(String key) {
        try {
            return resolve(key).toUri().toURL();
        } catch (Exception ex) {
            throw new StorageException("Falha ao gerar URL local (key=" + key + ")", ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
            log.info("Arquivo local removido: {}", key);
        } catch (Exception ex) {
            throw new StorageException("Falha ao remover arquivo local (key=" + key + ")", ex);
        }
    }

    private Path resolve(String key) {
        return baseDir.resolve(key).normalize();
    }
}
