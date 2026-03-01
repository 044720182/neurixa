package com.neurixa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.config.security.JwtTokenProvider;
import com.neurixa.config.security.TokenBlacklistService;
import com.neurixa.core.files.exception.FileValidationException;
import com.neurixa.core.usecase.GetUserByUsernameUseCase;
import com.neurixa.core.files.usecase.UploadFileUseCase;
import com.neurixa.core.files.usecase.CreateFolderUseCase;
import com.neurixa.core.files.usecase.DeleteFileUseCase;
import com.neurixa.core.files.usecase.ListFolderContentUseCase;
import com.neurixa.core.files.usecase.MoveFileUseCase;
import com.neurixa.core.files.usecase.RenameFileUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Constructor;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetUserByUsernameUseCase getUserByUsernameUseCase;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UploadFileUseCase uploadFileUseCase;

    @MockBean
    private CreateFolderUseCase createFolderUseCase;

    @MockBean
    private ListFolderContentUseCase listFolderContentUseCase;

    @MockBean
    private RenameFileUseCase renameFileUseCase;

    @MockBean
    private MoveFileUseCase moveFileUseCase;

    @MockBean
    private DeleteFileUseCase deleteFileUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private StoredFile testFile;

    @BeforeEach
    void setUp() {
        try {
            Constructor<User> constructor = User.class.getDeclaredConstructor(UserId.class, String.class, String.class, String.class, Role.class, boolean.class, boolean.class, int.class, Instant.class, Instant.class);
            constructor.setAccessible(true);
            testUser = constructor.newInstance(new UserId("user-123"), "testuser", "test@example.com", "hash", Role.USER, false, true, 0, Instant.now(), Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        testFile = StoredFile.createNew(testUser.getId(), "test.txt", "text/plain", 100L, null).markActive();
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUploadFileToRootSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        when(getUserByUsernameUseCase.execute("testuser")).thenReturn(testUser);
        when(uploadFileUseCase.execute(any(UserId.class), eq("test.txt"), eq("text/plain"), eq(12L), eq(null), any()))
                .thenReturn(testFile);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test.txt"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUploadFileToFolderSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        FolderId folderId = new FolderId("folder-123");
        StoredFile fileInFolder = StoredFile.createNew(testUser.getId(), "test.txt", "text/plain", 12L, folderId).markActive();
        when(getUserByUsernameUseCase.execute("testuser")).thenReturn(testUser);
        when(uploadFileUseCase.execute(any(UserId.class), eq("test.txt"), eq("text/plain"), eq(12L), eq(folderId), any()))
                .thenReturn(fileInFolder);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("folderId", "folder-123")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.folderId").value("folder-123"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnBadRequestWhenFileIsTooLarge() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "large.txt", "text/plain", "large content".getBytes());
        when(getUserByUsernameUseCase.execute("testuser")).thenReturn(testUser);
        when(uploadFileUseCase.execute(any(), any(), any(), any(Long.class), any(), any()))
                .thenThrow(new FileValidationException("File size exceeds the maximum allowed limit"));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
