package com.hrbank.hrbank.service;



import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.repository.FileMetaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileMetaServiceTest {

    @Mock
    private FileMetaRepository fileMetaRepository;

    @InjectMocks
    private FileMetaService fileMetaService;

    @Test
    @DisplayName("파일 메타데이터를 저장한다")
    void FileMetaData() {

        UUID fileId = UUID.randomUUID();
        FileMeta fileMeta = new FileMeta(fileId, "profile.png", "image/png", 1024L);

        when(fileMetaRepository.save(fileMeta)).thenReturn(fileMeta);

        FileMeta saved = fileMetaService.save(fileMeta);

        assertThat(saved).isEqualTo(fileMeta);
        verify(fileMetaRepository).save(fileMeta);



    }

    @Test
    @DisplayName("파일 ID로 메타데이터를 조회한다")
    void fileId() {

        UUID fileId = UUID.randomUUID();
        FileMeta fileMeta = new FileMeta(fileId, "profile.png", "image/png", 1024L);

        when(fileMetaRepository.findById(fileId)).thenReturn(Optional.of(fileMeta));

        Optional<FileMeta> found = fileMetaService.findById(fileId);

        assertThat(found).contains(fileMeta);
        verify(fileMetaRepository).findById(fileId);



    }


}
