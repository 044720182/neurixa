package com.neurixa.core.files.port;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;

import java.util.List;
import java.util.Optional;

public interface FolderRepository {
    Folder save(Folder folder);
    Optional<Folder> findByIdAndOwner(FolderId id, UserId ownerId);
    List<Folder> findChildren(UserId ownerId, FolderId parentId);
    List<Folder> findRoots(UserId ownerId);
    List<Folder> findChildren(UserId ownerId, FolderId parentId, int page, int size);
    List<Folder> findRoots(UserId ownerId, int page, int size);
    long countChildren(UserId ownerId, FolderId parentId);
    long countRoots(UserId ownerId);
}
