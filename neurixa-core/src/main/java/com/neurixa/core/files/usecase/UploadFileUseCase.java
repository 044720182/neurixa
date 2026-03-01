package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileVersion;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.exception.FileValidationException;
import com.neurixa.core.files.exception.FolderOwnershipException;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FileVersionRepository;
import com.neurixa.core.files.port.FolderRepository;
import com.neurixa.core.files.port.StorageProvider;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UploadFileUseCase {
    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FolderRepository folderRepository;
    private final StorageProvider storageProvider;
    private final Set<String> allowedMimeTypes;
    private final long maxFileSize;

    public UploadFileUseCase(FileRepository fileRepository,
                             FileVersionRepository fileVersionRepository,
                             FolderRepository folderRepository,
                             StorageProvider storageProvider,
                             Set<String> allowedMimeTypes,
                             long maxFileSize) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
        this.fileVersionRepository = Objects.requireNonNull(fileVersionRepository);
        this.folderRepository = Objects.requireNonNull(folderRepository);
        this.storageProvider = Objects.requireNonNull(storageProvider);
        this.allowedMimeTypes = Objects.requireNonNull(allowedMimeTypes);
        this.maxFileSize = maxFileSize;
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

        if (size > maxFileSize) {
            throw new FileValidationException("File size exceeds the maximum allowed limit");
        }
        if (!allowedMimeTypes.contains(mimeType)) {
            throw new FileValidationException("File MIME type is not allowed");
        }

        if (targetFolderId != null) {
            Optional<Folder> folderOpt = folderRepository.findByIdAndOwner(targetFolderId, ownerId);
            if (folderOpt.isEmpty() || folderOpt.get().isDeleted()) {
                throw new FolderOwnershipException("Folder not found or you don't have access");
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

