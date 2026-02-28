package com.neurixa.core.files.port;

import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.FileVersion;

import java.util.List;

public interface FileVersionRepository {
    FileVersion save(FileVersion version);
    List<FileVersion> findByFileId(FileId fileId);
}

