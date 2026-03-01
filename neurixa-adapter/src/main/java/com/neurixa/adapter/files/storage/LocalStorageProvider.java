package com.neurixa.adapter.files.storage;

import com.neurixa.adapter.files.config.StorageProperties;
import com.neurixa.core.files.port.StorageProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class LocalStorageProvider implements StorageProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageProvider.class);
    private final Path root;

    public LocalStorageProvider(StorageProperties properties) {
        this.root = Path.of(properties.getLocal().getRoot()).toAbsolutePath();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
            if (!Files.isWritable(root)) {
                throw new IOException("Storage root is not writable: " + root);
            }
            log.info("Initialized local storage at: {}", root);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize local storage at: " + root, e);
        }
    }

    @Override
    public String store(InputStream data, String filename) {
        try {
            String safeFilename = StringUtils.cleanPath(filename);
            if (safeFilename.contains("..")) {
                throw new IllegalArgumentException("Invalid filename");
            }
            LocalDate d = LocalDate.now();
            String key = d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/" + String.format("%02d", d.getDayOfMonth())
                    + "/" + UUID.randomUUID() + "-" + safeFilename;
            Path target = root.resolve(key);
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream retrieve(String storageKey) {
        try {
            Path p = root.resolve(storageKey).normalize();
            if (!p.startsWith(root)) {
                throw new SecurityException("Attempted to access file outside storage root");
            }
            return Files.newInputStream(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path p = root.resolve(storageKey).normalize();
            if (!p.startsWith(root)) {
                throw new SecurityException("Attempted to access file outside storage root");
            }
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

