package com.neurixa.adapter.files.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_versions")
public class FileVersionDocument {
    @Id
    private String id;
    @Indexed
    private String fileId;
    private int versionNumber;
    private String storageKey;
    private long size;
    private String checksumAlgorithm;
    private String checksumValue;
    @CreatedDate
    private Instant createdAt;
}

