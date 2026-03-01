package com.neurixa.adapter.files.persistence;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.FileStatus;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoFileRepository implements FileRepository {
    private final FileMongoRepository mongoRepository;

    @Override
    public StoredFile save(StoredFile file) {
        FileDocument doc = toDocument(file);
        FileDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<StoredFile> findByIdAndOwner(FileId id, UserId ownerId) {
        return mongoRepository.findById(id.getValue())
                .filter(d -> d.getOwnerId().equals(ownerId.getValue()))
                .map(this::toDomain);
    }

    @Override
    public List<StoredFile> findByFolder(UserId ownerId, FolderId folderId) {
        List<FileDocument> docs = folderId == null
                ? mongoRepository.findByOwnerIdAndFolderIdIsNull(ownerId.getValue())
                : mongoRepository.findByOwnerIdAndFolderId(ownerId.getValue(), folderId.getValue());
        return docs.stream().map(this::toDomain).toList();
    }

    private FileDocument toDocument(StoredFile f) {
        return FileDocument.builder()
                .id(f.getId().getValue())
                .ownerId(f.getOwnerId().getValue())
                .name(f.getName())
                .mimeType(f.getMimeType())
                .size(f.getSize())
                .folderId(f.getFolderId() != null ? f.getFolderId().getValue() : null)
                .status(f.getStatus())
                .currentVersion(f.getCurrentVersion())
                .deleted(f.isDeleted())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private StoredFile toDomain(FileDocument d) {
        return StoredFile.from(
                new FileId(d.getId()),
                new UserId(d.getOwnerId()),
                d.getName(),
                d.getMimeType(),
                d.getSize(),
                d.getFolderId() != null ? new FolderId(d.getFolderId()) : null,
                d.getStatus() != null ? d.getStatus() : FileStatus.ACTIVE,
                d.getCurrentVersion(),
                d.isDeleted(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}

