package com.vitalink.platform.service;

import com.vitalink.platform.service.impl.LocalFileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LocalFileStorageService")
class LocalFileStorageServiceTest {
    private final LocalFileStorageService service = new LocalFileStorageService();

    @Test
    @DisplayName("grava, gera URL local e remove o arquivo")
    void shouldUploadPresignAndDelete() throws Exception {
        String key = "patients/" + UUID.randomUUID() + "/exame.txt";
        byte[] content = "conteudo".getBytes(StandardCharsets.UTF_8);

        service.upload(key, content, "text/plain");

        URL url = service.generatePresignedDownloadUrl(key);
        Path stored = Paths.get(url.toURI());
        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.readAllBytes(stored)).isEqualTo(content);

        service.delete(key);
        assertThat(Files.exists(stored)).isFalse();
    }
}
