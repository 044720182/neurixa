package com.neurixa.core.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FileVersionRepository;
import com.neurixa.core.files.port.FolderRepository;
import com.neurixa.core.files.port.StorageProvider;
import com.neurixa.core.files.usecase.UploadFileUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadFileUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileVersionRepository fileVersionRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private StorageProvider storageProvider;

    private UploadFileUseCase useCase;

    private UserId ownerId;
    private String filename;
    private String mimeType;
    private long size;
    private InputStream data;

    @BeforeEach
    void setUp() {
        useCase = new UploadFileUseCase(fileRepository, fileVersionRepository, folderRepository, storageProvider);
        ownerId = new UserId("user-123");
        filename = "test.txt";
        mimeType = "text/plain";
        size = 100L;
        data = new ByteArrayInputStream("test content".getBytes());
    }

    @Test
    void shouldUploadFileToRootSuccessfully() {
        // Given
        String storageKey = "2026/03/01/uuid-test.txt";
        StoredFile savedFile = StoredFile.createNew(ownerId, filename, mimeType, size, null).markActive();
        when(storageProvider.store(any(InputStream.class), any(String.class))).thenReturn(storageKey);
        when(fileRepository.save(any(StoredFile.class))).thenReturn(savedFile);

        // When
        StoredFile result = useCase.execute(ownerId, filename, mimeType, size, null, data);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(filename);
        assertThat(result.getMimeType()).isEqualTo(mimeType);
        assertThat(result.getSize()).isEqualTo(size);
        assertThat(result.getFolderId()).isNull();
        verify(storageProvider).store(any(InputStream.class), any(String.class));
        verify(fileRepository).save(any(StoredFile.class));
        verify(fileVersionRepository).save(any());
    }

    @Test
    void shouldUploadFileToFolderSuccessfully() {
        // Given
        FolderId folderId = new FolderId("folder-123");
        Folder folder = Folder.from(folderId, ownerId, "Test Folder", null, "/folder-123", false, Instant.now(), Instant.now());
        String storageKey = "2026/03/01/uuid-test.txt";
        StoredFile savedFile = StoredFile.createNew(ownerId, filename, mimeType, size, folderId).markActive();
        when(folderRepository.findByIdAndOwner(folderId, ownerId)).thenReturn(Optional.of(folder));
        when(storageProvider.store(any(InputStream.class), any(String.class))).thenReturn(storageKey);
        when(fileRepository.save(any(StoredFile.class))).thenReturn(savedFile);

        // When
        StoredFile result = useCase.execute(ownerId, filename, mimeType, size, folderId, data);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFolderId()).isEqualTo(folderId);
        verify(folderRepository).findByIdAndOwner(folderId, ownerId);
    }

    @Test
    void shouldThrowExceptionWhenFolderNotFound() {
        // Given
        FolderId folderId = new FolderId("folder-123");
        when(folderRepository.findByIdAndOwner(folderId, ownerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(ownerId, filename, mimeType, size, folderId, data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder not found");
    }

    @Test
    void shouldThrowExceptionWhenFolderIsDeleted() {
        // Given
        FolderId folderId = new FolderId("folder-123");
        Folder deletedFolder = Folder.from(folderId, ownerId, "Test Folder", null, "/folder-123", true, Instant.now(), Instant.now());
        when(folderRepository.findByIdAndOwner(folderId, ownerId)).thenReturn(Optional.of(deletedFolder));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(ownerId, filename, mimeType, size, folderId, data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder not found");
    }

    @Test
    void shouldHandleNullInputs() {
        // When & Then
        assertThatThrownBy(() -> useCase.execute(null, filename, mimeType, size, null, data))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> useCase.execute(ownerId, null, mimeType, size, null, data))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> useCase.execute(ownerId, filename, mimeType, size, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
