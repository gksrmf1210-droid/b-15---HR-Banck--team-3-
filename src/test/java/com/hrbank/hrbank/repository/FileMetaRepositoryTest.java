package com.hrbank.hrbank.repository;

import com.hrbank.hrbank.entity.FileMeta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class FileMetaRepositoryTest {


    @Autowired
    private FileMetaRepository fileMetaRepository;

    @Test
    void 파일_메타데이터를_저장하고_조회한다() {
        // 테스트용 파일 ID 생성
        UUID fileId = UUID.randomUUID();

        // 파일 메타데이터 생성
        FileMeta fileMeta = new FileMeta(
                fileId,
                "profile.png",
                "image/png",
                1024L
        );

        // DB에 저장
        fileMetaRepository.save(fileMeta);

        // ID로 조회
        FileMeta found = fileMetaRepository.findById(fileId)
                .orElseThrow();

        // 저장한 값과 조회한 값 비교
        assertThat(found.getId()).isEqualTo(fileId);
        assertThat(found.getFileName()).isEqualTo("profile.png");
        assertThat(found.getContentType()).isEqualTo("image/png");
        assertThat(found.getSize()).isEqualTo(1024L);
    }



}
