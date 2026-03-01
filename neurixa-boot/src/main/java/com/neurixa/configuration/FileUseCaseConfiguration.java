package com.neurixa.configuration;

import com.neurixa.adapter.files.config.StorageProperties;
import com.neurixa.core.files.port.FileRepository;
import com.neurixa.core.files.port.FileVersionRepository;
import com.neurixa.core.files.port.FolderRepository;
import com.neurixa.core.files.port.StorageProvider;
import com.neurixa.core.files.usecase.CreateFolderUseCase;
import com.neurixa.core.files.usecase.DeleteFileUseCase;
import com.neurixa.core.files.usecase.ListFolderContentUseCase;
import com.neurixa.core.files.usecase.ListFolderContentPagedUseCase;
import com.neurixa.core.files.usecase.MoveFileUseCase;
import com.neurixa.core.files.usecase.RenameFileUseCase;
import com.neurixa.core.files.usecase.UploadFileUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUseCaseConfiguration {
    @Bean
    public UploadFileUseCase uploadFileUseCase(FileRepository fileRepository,
                                               FileVersionRepository fileVersionRepository,
                                               FolderRepository folderRepository,
                                               StorageProvider storageProvider,
                                               StorageProperties storageProperties) {
        return new UploadFileUseCase(fileRepository, fileVersionRepository, folderRepository, storageProvider,
                storageProperties.getAllowedMimeTypes(), storageProperties.getMaxFileSize());
    }

    @Bean
    public CreateFolderUseCase createFolderUseCase(FolderRepository folderRepository) {
        return new CreateFolderUseCase(folderRepository);
    }

    @Bean
    public ListFolderContentUseCase listFolderContentUseCase(FileRepository fileRepository, FolderRepository folderRepository) {
        return new ListFolderContentUseCase(fileRepository, folderRepository);
    }

    @Bean
    public ListFolderContentPagedUseCase listFolderContentPagedUseCase(FileRepository fileRepository, FolderRepository folderRepository) {
        return new ListFolderContentPagedUseCase(fileRepository, folderRepository);
    }

    @Bean
    public RenameFileUseCase renameFileUseCase(FileRepository fileRepository) {
        return new RenameFileUseCase(fileRepository);
    }

    @Bean
    public MoveFileUseCase moveFileUseCase(FileRepository fileRepository, FolderRepository folderRepository) {
        return new MoveFileUseCase(fileRepository, folderRepository);
    }

    @Bean
    public DeleteFileUseCase deleteFileUseCase(FileRepository fileRepository) {
        return new DeleteFileUseCase(fileRepository);
    }
}
