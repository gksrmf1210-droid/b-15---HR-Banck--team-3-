package com.hrbank.hrbank.service;



import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.repository.FileMetaRepository;
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
    void 파일_메타데이터를_저장한다() {

        UUID fileId = UUID.randomUUID();
        FileMeta fileMeta = new FileMeta(fileId, "profile.png", "image/png", 1024L);

        when(fileMetaRepository.save(fileMeta)).thenReturn(fileMeta);

        FileMeta saved = fileMetaService.save(fileMeta);

        assertThat(saved).isEqualTo(fileMeta);
        verify(fileMetaRepository).save(fileMeta);



    }

    @Test
    void 파일_ID로_메타데이터를_조회한다() {

        UUID fileId = UUID.randomUUID();
        FileMeta fileMeta = new FileMeta(fileId, "profile.png", "image/png", 1024L);

        when(fileMetaRepository.findById(fileId)).thenReturn(Optional.of(fileMeta));

        Optional<FileMeta> found = fileMetaService.findById(fileId);

        assertThat(found).contains(fileMeta);
        verify(fileMetaRepository).findById(fileId);

    }


}
