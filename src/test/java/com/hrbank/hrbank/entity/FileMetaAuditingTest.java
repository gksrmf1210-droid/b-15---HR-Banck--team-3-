package com.hrbank.hrbank.entity;


import com.hrbank.hrbank.file.entity.FileMeta;
import com.hrbank.hrbank.file.repository.FileMetaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class FileMetaAuditingTest {

    @Autowired
    private FileMetaRepository fileMetaRepository;

    @Test
    @DisplayName("파일 생성시 수정시간이 자동으로 기록된다")
    void createdAndUpdatedAtAreAutomaticallyRecordedOnCreate () {

        FileMeta fileMeta = new FileMeta(
                UUID.randomUUID(),
                "test.txt",
                "text/plain",
                100L
        );

        FileMeta saved = fileMetaRepository.saveAndFlush(fileMeta);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
