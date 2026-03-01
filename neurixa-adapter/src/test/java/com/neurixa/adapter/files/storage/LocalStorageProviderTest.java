package com.neurixa.adapter.files.storage;

import com.neurixa.adapter.files.config.StorageProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStorageProviderTest {

    @TempDir
    Path tempDir;

    private LocalStorageProvider storageProvider;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.getLocal().setRoot(tempDir.toString());
        storageProvider = new LocalStorageProvider(properties);
        storageProvider.init();
    }

    @Test
    void shouldStoreAndRetrieveFile() throws IOException {
        // Given
        String filename = "test.txt";
        String content = "test content";
        InputStream data = new ByteArrayInputStream(content.getBytes());

        // When
        String storageKey = storageProvider.store(data, filename);
        InputStream retrieved = storageProvider.retrieve(storageKey);

        // Then
        assertThat(storageKey).isNotNull();
        assertThat(new String(retrieved.readAllBytes())).isEqualTo(content);
    }

    @Test
    void shouldDeleteFile() {
        // Given
        String filename = "test.txt";
        String content = "test content";
        InputStream data = new ByteArrayInputStream(content.getBytes());
        String storageKey = storageProvider.store(data, filename);

        // When
        storageProvider.delete(storageKey);

        // Then
        assertThat(Files.exists(tempDir.resolve(storageKey))).isFalse();
    }
}
