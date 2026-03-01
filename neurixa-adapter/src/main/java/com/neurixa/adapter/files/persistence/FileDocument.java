package com.neurixa.adapter.files.persistence;

import com.neurixa.core.files.domain.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")
public class FileDocument {
    @Id
    private String id;
    @Indexed
    private String ownerId;
    private String name;
    private String mimeType;
    private long size;
    @Indexed
    private String folderId;
    @Indexed
    private FileStatus status;
    private int currentVersion;
    private boolean deleted;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}

