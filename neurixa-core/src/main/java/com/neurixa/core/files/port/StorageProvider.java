package com.neurixa.core.files.port;

import java.io.InputStream;

public interface StorageProvider {
    String store(InputStream data, String filename);
    InputStream retrieve(String storageKey);
    void delete(String storageKey);
}

