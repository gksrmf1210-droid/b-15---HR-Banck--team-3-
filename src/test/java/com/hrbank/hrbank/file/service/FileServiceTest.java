package com.hrbank.hrbank.file.service;


import com.hrbank.hrbank.file.entity.FileMeta;
import com.hrbank.hrbank.file.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.hrbank.hrbank.global.exception.BusinessException;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileServiceTest {

    @TempDir
    Path tempDir;

    private FileStorage fileStorage;
    private FileMetaService fileMetaService;
    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileStorage = mock(FileStorage.class);
        fileMetaService = mock(FileMetaService.class);

        fileService = new FileService(
                fileStorage,
                fileMetaService
        );
    }

    @Test
    void uploadStoresFileAndSavesMetadata() throws IOException {
        UUID storageKey = UUID.randomUUID();
        String fileName = "test.txt";
        String contentType = "text/plain";
        byte[] content =
                "HR Bank Test".getBytes(StandardCharsets.UTF_8);

        when(fileStorage.store(any(), eq(fileName)))
                .thenReturn(storageKey);

        when(fileMetaService.save(any(FileMeta.class)))
                .thenAnswer(invocation -> {
                    FileMeta saved = invocation.getArgument(0);
                    return saved;
                });

        FileMeta result = fileService.upload(
                new ByteArrayInputStream(content),
                fileName,
                contentType,
                content.length
        );

        assertEquals(storageKey, result.getStorageKey());
        assertEquals(fileName, result.getFileName());
        assertEquals(contentType, result.getContentType());
        assertEquals(content.length, result.getSize());
    }

    @Test
    void uploadDeletesStoredFileWhenMetadataSaveFails() throws IOException {
        UUID storageKey = UUID.randomUUID();

        when(fileStorage.store(any(), eq("test.txt")))
                .thenReturn(storageKey);

        when(fileMetaService.save(any(FileMeta.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        assertThrows(
                RuntimeException.class,
                () -> fileService.upload(
                        new ByteArrayInputStream(
                                "test".getBytes(StandardCharsets.UTF_8)
                        ),
                        "test.txt",
                        "text/plain",
                        4
                )
        );

        verify(fileStorage).delete(storageKey);
    }

    @Test
    void downloadReturnsFileContent() throws IOException {
        Long fileId = 1L;
        UUID storageKey = UUID.randomUUID();

        FileMeta fileMeta = new FileMeta(
                storageKey,
                "test.txt",
                "text/plain",
                4
        );

        InputStream expectedStream = new ByteArrayInputStream(
                "test".getBytes(StandardCharsets.UTF_8)
        );

        when(fileMetaService.findById(fileId))
                .thenReturn(java.util.Optional.of(fileMeta));

        when(fileStorage.load(storageKey))
                .thenReturn(expectedStream);

        InputStream result = fileService.download(fileId);

        assertSame(expectedStream, result);

        verify(fileMetaService).findById(fileId);
        verify(fileStorage).load(storageKey);
    }

    @Test
    void downloadThrowsExceptionWhenMetadataDoesNotExist() throws IOException {
        Long fileId = 1L;

        when(fileMetaService.findById(fileId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> fileService.download(fileId)
        );

        verify(fileMetaService).findById(fileId);
        verify(fileStorage, never()).load(any(UUID.class));
    }

    @Test
    void deleteRemovesFileAndMetadata() throws IOException {
        Long fileId = 1L;
        UUID storageKey = UUID.randomUUID();

        FileMeta fileMeta = new FileMeta(
                storageKey,
                "test.txt",
                "text/plain",
                4
        );

        when(fileMetaService.findById(fileId))
                .thenReturn(java.util.Optional.of(fileMeta));

        fileService.delete(fileId);

        verify(fileStorage).delete(storageKey);
        verify(fileMetaService).deleteById(fileId);
    }

    @Test
    void deleteThrowsExceptionWhenMetadataDoesNotExist() throws IOException {
        Long fileId = 1L;

        when(fileMetaService.findById(fileId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> fileService.delete(fileId)
        );

        verify(fileStorage, never()).delete(any(UUID.class));
        verify(fileMetaService, never()).deleteById(fileId);
    }

}






