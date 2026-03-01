package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.port.FolderRepository;

import java.util.Objects;
import java.util.Optional;

public class CreateFolderUseCase {
    private final FolderRepository folderRepository;

    public CreateFolderUseCase(FolderRepository folderRepository) {
        this.folderRepository = Objects.requireNonNull(folderRepository);
    }

    public Folder execute(UserId ownerId, String name, FolderId parentId) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(name);

        if (parentId == null) {
            Folder folder = Folder.createRoot(ownerId, name);
            return folderRepository.save(folder);
        }

        Optional<Folder> parent = folderRepository.findByIdAndOwner(parentId, ownerId);
        if (parent.isEmpty() || parent.get().isDeleted()) {
            throw new IllegalArgumentException("Parent folder not found");
        }
        Folder folder = Folder.createChild(ownerId, name, parent.get());
        return folderRepository.save(folder);
    }
}

