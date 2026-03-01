package com.neurixa.core.files.domain;

import java.util.List;

public record FolderContentPaged(
        List<Folder> folders,
        long totalFolders,
        List<StoredFile> files,
        long totalFiles
) {}
