package com.neurixa.controller;

import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.usecase.GetUserByUsernameUseCase;
import com.neurixa.core.files.domain.FileId;
import com.neurixa.core.files.domain.Folder;
import com.neurixa.core.files.domain.FolderContent;
import com.neurixa.core.files.domain.FolderId;
import com.neurixa.core.files.domain.StoredFile;
import com.neurixa.core.files.usecase.CreateFolderUseCase;
import com.neurixa.core.files.usecase.DeleteFileUseCase;
import com.neurixa.core.files.usecase.ListFolderContentUseCase;
import com.neurixa.core.files.usecase.MoveFileUseCase;
import com.neurixa.core.files.usecase.RenameFileUseCase;
import com.neurixa.core.files.usecase.UploadFileUseCase;
import com.neurixa.dto.request.CreateFolderRequest;
import com.neurixa.dto.request.MoveFileRequest;
import com.neurixa.dto.request.RenameFileRequest;
import com.neurixa.dto.response.FileResponse;
import com.neurixa.dto.response.FolderContentResponse;
import com.neurixa.dto.response.FolderResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final GetUserByUsernameUseCase getUserByUsernameUseCase;
    private final UploadFileUseCase uploadFileUseCase;
    private final CreateFolderUseCase createFolderUseCase;
    private final ListFolderContentUseCase listFolderContentUseCase;
    private final RenameFileUseCase renameFileUseCase;
    private final MoveFileUseCase moveFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;

    @PostMapping(path = "/files/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<FileResponse> upload(@RequestPart("file") MultipartFile file,
                                               @RequestParam(value = "folderId", required = false) String folderId,
                                               Principal principal) throws IOException {
        User user = getUserByUsernameUseCase.execute(principal.getName());
        UserId ownerId = user.getId();
        FolderId parent = folderId != null && !folderId.isBlank() ? new FolderId(folderId) : null;
        try (InputStream is = file.getInputStream()) {
            StoredFile stored = uploadFileUseCase.execute(
                    ownerId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    parent,
                    is
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toFileResponse(stored));
        }
    }

    @PostMapping("/folders")
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody CreateFolderRequest request, Principal principal) {
        User user = getUserByUsernameUseCase.execute(principal.getName());
        UserId ownerId = user.getId();
        FolderId parent = request.parentId() != null && !request.parentId().isBlank() ? new FolderId(request.parentId()) : null;
        Folder folder = createFolderUseCase.execute(ownerId, request.name(), parent);
        return ResponseEntity.status(HttpStatus.CREATED).body(toFolderResponse(folder));
    }

    @GetMapping("/folders/contents")
    public ResponseEntity<FolderContentResponse> listContents(@RequestParam(value = "parentId", required = false) String parentId,
                                                              Principal principal) {
        User user = getUserByUsernameUseCase.execute(principal.getName());
        UserId ownerId = user.getId();
        FolderId parent = parentId != null && !parentId.isBlank() ? new FolderId(parentId) : null;
        FolderContent content = listFolderContentUseCase.execute(ownerId, parent);
        List<FolderResponse> folders = content.folders().stream().map(this::toFolderResponse).toList();
        List<FileResponse> files = content.files().stream().map(this::toFileResponse).toList();
        return ResponseEntity.ok(new FolderContentResponse(folders, files));
    }

    @PutMapping("/files/{id}/rename")
    public ResponseEntity<FileResponse> rename(@PathVariable String id, @Valid @RequestBody RenameFileRequest request, Principal principal) {
        User user = getUserByUsernameUseCase.execute(principal.getName());
        StoredFile updated = renameFileUseCase.execute(user.getId(), new FileId(id), request.name());
        return ResponseEntity.ok(toFileResponse(updated));
    }

    @PutMapping("/files/{id}/move")
    public ResponseEntity<FileResponse> move(@PathVariable String id, @RequestBody MoveFileRequest request, Principal principal) {
        User user = getUserByUsernameUseCase.execute(principal.getName());
        FolderId target = request.targetFolderId() != null && !request.targetFolderId().isBlank() ? new FolderId(request.targetFolderId()) : null;
        StoredFile updated = moveFileUseCase.execute(user.getId(), new FileId(id), target);
        return ResponseEntity.ok(toFileResponse(updated));
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Principal principal) {
        User user = getUserByUsernameUseCase.execute(principal.getName());
        deleteFileUseCase.execute(user.getId(), new FileId(id));
        return ResponseEntity.noContent().build();
    }

    private FileResponse toFileResponse(StoredFile f) {
        return new FileResponse(
                f.getId().getValue(),
                f.getName(),
                f.getMimeType(),
                f.getSize(),
                f.getFolderId() != null ? f.getFolderId().getValue() : null,
                f.getStatus(),
                f.getCurrentVersion(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }

    private FolderResponse toFolderResponse(Folder folder) {
        return new FolderResponse(
                folder.getId().getValue(),
                folder.getName(),
                folder.getParentId() != null ? folder.getParentId().getValue() : null,
                folder.getPath(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}

