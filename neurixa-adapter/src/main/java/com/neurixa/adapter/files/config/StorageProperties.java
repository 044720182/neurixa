package com.neurixa.adapter.files.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "neurixa.storage")
@Validated
public class StorageProperties {

    private LocalStorageProperties local = new LocalStorageProperties();
    private Set<String> allowedMimeTypes = Set.of("image/jpeg", "image/png", "application/pdf");
    private long maxFileSize = 10485760; // 10MB

    public LocalStorageProperties getLocal() {
        return local;
    }

    public void setLocal(LocalStorageProperties local) {
        this.local = local;
    }

    public Set<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(Set<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public static class LocalStorageProperties {
        @NotBlank(message = "Storage root path must be configured")
        private String root = "/tmp/neurixa-storage";

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }
    }
}
