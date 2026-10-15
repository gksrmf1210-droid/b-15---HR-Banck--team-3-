package com.hrbank.hrbank.databackup;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupHistoryRepository extends JpaRepository<BackupHistoryEntity, Long> {


  Optional<BackupHistoryEntity> findTopByStatusOrderByEndedAtDesc(BackupStatus status);
}
