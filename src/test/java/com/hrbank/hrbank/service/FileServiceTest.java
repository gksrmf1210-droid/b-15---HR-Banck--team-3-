package com.hrbank.hrbank.service;


import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static com.hrbank.hrbank.entity.QFileMeta.fileMeta;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileServiceTest {

    @TempDir
    Path tempDir;

    private  FileStorage fileStorage;
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

        UUID fileId = UUID.randomUUID();
        String fileName = "test.txt";
        String contentType = "text/plain";
        byte[] content =
                "HR Bank Test".getBytes(StandardCharsets.UTF_8);

        when(fileStorage.store(any(), eq(fileName))).thenReturn(fileId);

       when(fileMetaService.save(any(FileMeta.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

       FileMeta result = fileService.upload(
               new ByteArrayInputStream(content),
               fileName,
               contentType,
               content.length
       );

       assertEquals(fileId, result.getId());
       assertEquals(fileName, result.getFileName());
       assertEquals(contentType, result.getContentType());
       assertEquals(content.length, result.getSize());



    }

    @Test
    void uploadDeletesStoredFileWhenMetadataSaveFails() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();

        when(fileStorage.store(any(), eq("test.txt")))
                .thenReturn(fileId);

        when(fileMetaService.save(any(FileMeta.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        // when & then
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

        verify(fileStorage).delete(fileId);






    }

    @Test
    void downloadReturnsFileContent() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();

        FileMeta fileMeta = new FileMeta(
                fileId,
                "test.txt",
                "text/plain",
                4
        );

        InputStream expectedStream = new ByteArrayInputStream(
                "test".getBytes(StandardCharsets.UTF_8)
        );

        when(fileMetaService.findById(fileId))
                .thenReturn(java.util.Optional.of(fileMeta));

        when(fileStorage.load(fileId))
                .thenReturn(expectedStream);

        // when
        InputStream result = fileService.download(fileId);

        // then
        assertSame(expectedStream, result);

        verify(fileMetaService).findById(fileId);
        verify(fileStorage).load(fileId);
    }

    @Test
    void downloadThrowsExceptionWhenMetadataDoesNotExist() throws IOException {
        // given
        UUID fileId = UUID.randomUUID();

        when(fileMetaService.findById(fileId))
                .thenReturn(java.util.Optional.empty());

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> fileService.download(fileId)
        );

        verify(fileMetaService).findById(fileId);
        verify(fileStorage, never()).load(fileId);
    }



}






