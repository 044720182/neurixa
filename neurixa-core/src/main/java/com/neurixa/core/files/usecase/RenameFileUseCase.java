package com.neurixa.core.files.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;

import java.util.Objects;

public class RenameFileUseCase {
    private final FileRepository fileRepository;

    public RenameFileUseCase(FileRepository fileRepository) {
        this.fileRepository = Objects.requireNonNull(fileRepository);
    }

    public StoredFile execute(UserId ownerId, FileId fileId, String newName) {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(fileId);
        Objects.requireNonNull(newName);

        StoredFile file = fileRepository.findByIdAndOwner(fileId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        StoredFile updated = file.rename(newName);
        return fileRepository.save(updated);
    }
}

