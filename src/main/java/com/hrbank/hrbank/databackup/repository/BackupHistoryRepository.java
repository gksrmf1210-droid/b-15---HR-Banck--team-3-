package com.hrbank.hrbank.databackup.repository;

import com.hrbank.hrbank.databackup.entity.BackupHistoryEntity;
import com.hrbank.hrbank.databackup.BackupStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BackupHistoryRepository extends JpaRepository<BackupHistoryEntity, Long>,
    BackupHistoryRepositoryCustom {

  Optional<BackupHistoryEntity> findTopByStatusOrderByEndedAtDesc(BackupStatus status);

  Optional<BackupHistoryEntity> findTopByOrderByIdDesc();

  boolean existsByStatus(BackupStatus backupStatus);
}