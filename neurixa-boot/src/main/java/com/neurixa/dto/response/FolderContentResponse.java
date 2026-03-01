package com.neurixa.dto.response;

import java.util.List;

public record FolderContentResponse(
        List<FolderResponse> folders,
        List<FileResponse> files
) {}

