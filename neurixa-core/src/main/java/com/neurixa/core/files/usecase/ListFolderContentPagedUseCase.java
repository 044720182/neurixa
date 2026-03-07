package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderContentPaged;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FolderRepository;

import java.util.List;
import java.util.Objects;

public class ListFolderContentPagedUseCase {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public ListFolderContentPagedUseCase(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
        this.folderRepository = Objects.requireNonNull(folderRepository);
    }

    public FolderContentPaged execute(UserId ownerId, FolderId folderId, int pageFolders, int sizeFolders, int pageFiles, int sizeFiles) {
        Objects.requireNonNull(ownerId);
        if (pageFolders < 0) pageFolders = 0;
        if (sizeFolders <= 0 || sizeFolders > 100) sizeFolders = 20;
        if (pageFiles < 0) pageFiles = 0;
        if (sizeFiles <= 0 || sizeFiles > 100) sizeFiles = 20;

        if (folderId != null) {
            Folder folder = folderRepository.findByIdAndOwner(folderId, ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
            if (folder.isDeleted()) {
                throw new IllegalArgumentException("Folder not found");
            }
        }

        List<Folder> folders = folderId == null
                ? folderRepository.findRoots(ownerId, pageFolders, sizeFolders)
                : folderRepository.findChildren(ownerId, folderId, pageFolders, sizeFolders);
        long totalFolders = folderId == null
                ? folderRepository.countRoots(ownerId)
                : folderRepository.countChildren(ownerId, folderId);

        List<StoredFile> files = fileRepository.findByFolder(ownerId, folderId, pageFiles, sizeFiles);
        long totalFiles = fileRepository.countByFolder(ownerId, folderId);

        return new FolderContentPaged(folders, totalFolders, files, totalFiles);
    }
}
