package com.neurixa.adapter.files.persistence;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.port.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoFolderRepository implements FolderRepository {
    private final FolderMongoRepository mongoRepository;

    @Override
    public Folder save(Folder folder) {
        FolderDocument doc = toDocument(folder);
        FolderDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<Folder> findByIdAndOwner(FolderId id, UserId ownerId) {
        return mongoRepository.findById(id.getValue())
                .filter(d -> d.getOwnerId().equals(ownerId.getValue()))
                .map(this::toDomain);
    }

    @Override
    public List<Folder> findChildren(UserId ownerId, FolderId parentId) {
        List<FolderDocument> docs = mongoRepository.findByOwnerIdAndParentId(ownerId.getValue(), parentId.getValue());
        return docs.stream().map(this::toDomain).toList();
    }

    @Override
    public List<Folder> findRoots(UserId ownerId) {
        List<FolderDocument> docs = mongoRepository.findByOwnerIdAndParentIdIsNull(ownerId.getValue());
        return docs.stream().map(this::toDomain).toList();
    }

    private FolderDocument toDocument(Folder folder) {
        return FolderDocument.builder()
                .id(folder.getId().getValue())
                .ownerId(folder.getOwnerId().getValue())
                .name(folder.getName())
                .parentId(folder.getParentId() != null ? folder.getParentId().getValue() : null)
                .path(folder.getPath())
                .deleted(folder.isDeleted())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }

    private Folder toDomain(FolderDocument d) {
        return Folder.from(
                new FolderId(d.getId()),
                new UserId(d.getOwnerId()),
                d.getName(),
                d.getParentId() != null ? new FolderId(d.getParentId()) : null,
                d.getPath(),
                d.isDeleted(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
