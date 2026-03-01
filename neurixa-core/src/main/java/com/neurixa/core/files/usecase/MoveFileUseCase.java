package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FolderRepository;

import java.util.Objects;

public class MoveFileUseCase {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public MoveFileUseCase(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
        this.folderRepository = Objects.requireNonNull(folderRepository);
    }

    public StoredFile execute(UserId ownerId, FileId fileId, FolderId targetFolderId) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(fileId);

        StoredFile file = fileRepository.findByIdAndOwner(fileId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (targetFolderId != null) {
            Folder target = folderRepository.findByIdAndOwner(targetFolderId, ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Target folder not found"));
            if (target.isDeleted()) {
                throw new IllegalArgumentException("Target folder not found");
            }
        }

        StoredFile updated = file.move(targetFolderId);
        return fileRepository.save(updated);
    }
}

