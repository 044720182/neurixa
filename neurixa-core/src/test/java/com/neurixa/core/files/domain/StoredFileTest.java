package com.neurixa.core.files.domain;

import com.neurixa.core.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoredFileTest {

    private StoredFile file;
    private UserId ownerId;

    @BeforeEach
    void setUp() {
        ownerId = new UserId("user-123");
        file = StoredFile.createNew(ownerId, "test.txt", "text/plain", 100L, null);
    }

    @Test
    void shouldCreateNewFileWithUploadingStatus() {
        assertThat(file.getStatus()).isEqualTo(FileStatus.UPLOADING);
        assertThat(file.getCurrentVersion()).isEqualTo(1);
        assertThat(file.isDeleted()).isFalse();
    }

    @Test
    void shouldTransitionToActiveStatus() {
        StoredFile activeFile = file.markActive();
        assertThat(activeFile.getStatus()).isEqualTo(FileStatus.ACTIVE);
    }

    @Test
    void shouldIncrementVersion() {
        StoredFile newVersion = file.incrementVersion();
        assertThat(newVersion.getCurrentVersion()).isEqualTo(2);
    }

    @Test
    void shouldMarkAsDeleted() {
        StoredFile deletedFile = file.markDeleted();
        assertThat(deletedFile.getStatus()).isEqualTo(FileStatus.DELETED);
        assertThat(deletedFile.isDeleted()).isTrue();
    }
}
