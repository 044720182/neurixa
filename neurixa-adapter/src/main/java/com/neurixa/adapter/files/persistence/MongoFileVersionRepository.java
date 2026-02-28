package com.neurixa.adapter.files.persistence;

import com.neurixa.core.files.domain.Checksum;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.FileVersion;
import com.neurixa.core.files.domain.FileVersionId;
import com.neurixa.core.files.port.FileVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MongoFileVersionRepository implements FileVersionRepository {
    private final FileVersionMongoRepository mongoRepository;

    @Override
    public FileVersion save(FileVersion version) {
        FileVersionDocument doc = toDocument(version);
        FileVersionDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<FileVersion> findByFileId(FileId fileId) {
        return mongoRepository.findByFileId(fileId.getValue()).stream().map(this::toDomain).toList();
    }

    private FileVersionDocument toDocument(FileVersion v) {
        return FileVersionDocument.builder()
                .id(v.getId().getValue())
                .fileId(v.getFileId().getValue())
                .versionNumber(v.getVersionNumber())
                .storageKey(v.getStorageKey())
                .size(v.getSize())
                .checksumAlgorithm(v.getChecksum() != null ? v.getChecksum().getAlgorithm() : null)
                .checksumValue(v.getChecksum() != null ? v.getChecksum().getValue() : null)
                .createdAt(v.getCreatedAt())
                .build();
    }

    private FileVersion toDomain(FileVersionDocument d) {
        Checksum checksum = null;
        if (d.getChecksumAlgorithm() != null && d.getChecksumValue() != null) {
            checksum = new Checksum(d.getChecksumAlgorithm(), d.getChecksumValue());
        }
        return FileVersion.from(
                new FileVersionId(d.getId()),
                new FileId(d.getFileId()),
                d.getVersionNumber(),
                d.getStorageKey(),
                d.getSize(),
                checksum,
                d.getCreatedAt()
        );
    }
}

