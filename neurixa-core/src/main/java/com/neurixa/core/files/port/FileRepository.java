package com.neurixa.core.files.port;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;

import java.util.List;
import java.util.Optional;

public interface FileRepository {
    StoredFile save(StoredFile file);
    Optional<StoredFile> findByIdAndOwner(FileId id, UserId ownerId);
    List<StoredFile> findByFolder(UserId ownerId, FolderId folderId);
}

