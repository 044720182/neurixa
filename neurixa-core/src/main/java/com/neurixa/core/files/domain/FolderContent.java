package com.neurixa.core.files.domain;

import java.util.List;

public record FolderContent(List<Folder> folders, List<StoredFile> files) {}

