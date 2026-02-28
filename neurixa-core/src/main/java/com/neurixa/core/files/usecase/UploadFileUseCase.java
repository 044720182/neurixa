package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileVersion;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FileVersionRepository;
import com.neurixa.core.files.port.FolderRepository;
import com.neurixa.core.files.port.StorageProvider;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

public class UploadFileUseCase {
    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FolderRepository folderRepository;
    private final StorageProvider storageProvider;

    public UploadFileUseCase(FileRepository fileRepository,
                             FileVersionRepository fileVersionRepository,
                             FolderRepository folderRepository,
                             StorageProvider storageProvider) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
        this.fileVersionRepository = Objects.requireNonNull(fileVersionRepository);
        this.folderRepository = Objects.requireNonNull(folderRepository);
        this.storageProvider = Objects.requireNonNull(storageProvider);
    }

    public StoredFile execute(UserId ownerId,
                              String originalFilename,
                              String mimeType,
                              long size,
                              FolderId targetFolderId,
                              InputStream data) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(originalFilename);
        Objects.requireNonNull(data);

        if (targetFolderId != null) {
            Optional<Folder> folderOpt = folderRepository.findByIdAndOwner(targetFolderId, ownerId);
            if (folderOpt.isEmpty() || folderOpt.get().isDeleted()) {
                throw new IllegalArgumentException("Folder not found");
            }
        }

        String storageKey = storageProvider.store(data, originalFilename);
        StoredFile file = StoredFile.createNew(ownerId, originalFilename, mimeType, size, targetFolderId).markActive();
        StoredFile saved = fileRepository.save(file);
        FileVersion version = FileVersion.createNew(saved.getId(), 1, storageKey, size, null);
        fileVersionRepository.save(version);
        return saved;
    }
}

