package com.neurixa.adapter.files.storage;

import com.neurixa.core.files.port.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    private final Path root;

    public LocalStorageProvider(@Value("${storage.local.root:#{null}}") String rootPath) {
        String base = rootPath != null && !rootPath.isBlank()
                ? rootPath
                : System.getProperty("user.home") + "/neurixa-storage";
        this.root = Path.of(base);
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String store(InputStream data, String filename) {
        String safeName = sanitize(filename);
        LocalDate d = LocalDate.now();
        String key = d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/" + String.format("%02d", d.getDayOfMonth())
                + "/" + UUID.randomUUID() + "-" + safeName;
        Path target = root.resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream retrieve(String storageKey) {
        Path p = root.resolve(storageKey);
        try {
            return Files.newInputStream(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path p = root.resolve(storageKey);
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String sanitize(String name) {
        String n = name == null ? "file" : name.trim();
        n = n.replaceAll("[\\r\\n]", "_");
        if (n.isBlank()) {
            n = "file";
        }
        return n;
    }
}

