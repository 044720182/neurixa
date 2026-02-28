package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;

import java.util.Objects;

public class DeleteFileUseCase {
    private final FileRepository fileRepository;

    public DeleteFileUseCase(FileRepository fileRepository) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
    }

    public StoredFile execute(UserId ownerId, FileId fileId) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(fileId);

        StoredFile file = fileRepository.findByIdAndOwner(fileId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        StoredFile updated = file.markDeleted();
        return fileRepository.save(updated);
    }
}

