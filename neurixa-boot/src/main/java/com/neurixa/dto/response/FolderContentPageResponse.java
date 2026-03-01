package com.neurixa.dto.response;

public record FolderContentPageResponse(
        PageResponse<FolderResponse> folders,
        PageResponse<FileResponse> files
) {}
