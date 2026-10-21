package com.hrbank.hrbank.file.repository;

import com.hrbank.hrbank.file.entity.FileMeta;
import com.hrbank.hrbank.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;



@DataJpaTest
@Import(QuerydslConfig.class)
public class FileMetaRepositoryTest {


    @Autowired
    private FileMetaRepository fileMetaRepository;

    @Test
    void 파일_메타데이터를_저장하고_조회한다() {

        FileMeta fileMeta = new FileMeta(
                java.util.UUID.randomUUID(),
                "profile.png",
                "image/png",
                1024L
        );


        FileMeta saved = fileMetaRepository.save(fileMeta);


        Long fileId = saved.getId();
        FileMeta found = fileMetaRepository.findById(fileId)
                .orElseThrow();


        assertThat(found.getId()).isEqualTo(fileId);
        assertThat(found.getFileName()).isEqualTo("profile.png");
        assertThat(found.getContentType()).isEqualTo("image/png");
        assertThat(found.getSize()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("저장하면 생성 시각과 수정 시각이 자동 설정된다.")
    void shouldSetCreatedAtAndUpdatedAtAutomaticallyWhenSaved() {

        FileMeta fileMeta = new FileMeta(
                UUID.randomUUID(),
                "profile.png",
                "image/png",
                1024L
        );

        FileMeta saved = fileMetaRepository.saveAndFlush(fileMeta);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

    }



}
