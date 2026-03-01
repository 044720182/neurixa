package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderContent;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FolderRepository;

import java.util.List;
import java.util.Objects;

public class ListFolderContentUseCase {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public ListFolderContentUseCase(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
        this.folderRepository = Objects.requireNonNull(folderRepository);
    }

    public FolderContent execute(UserId ownerId, FolderId folderId) {
        Objects.requireNonNull(ownerId);
        if (folderId != null) {
            Folder folder = folderRepository.findByIdAndOwner(folderId, ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
            if (folder.isDeleted()) {
                throw new IllegalArgumentException("Folder not found");
            }
        }
        List<Folder> folders = folderId == null ? folderRepository.findRoots(ownerId) : folderRepository.findChildren(ownerId, folderId);
        List<StoredFile> files = fileRepository.findByFolder(ownerId, folderId);
        return new FolderContent(folders, files);
    }
}

