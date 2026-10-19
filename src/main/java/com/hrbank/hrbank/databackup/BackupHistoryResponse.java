package com.hrbank.hrbank.databackup;

import java.time.LocalDateTime;
import java.util.UUID;

public record BackupHistoryResponse(
    Long id,
    String worker,
    BackupStatus status,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    UUID backupFileId
) {
  public static BackupHistoryResponse from(BackupHistoryEntity entity) {
    return new BackupHistoryResponse(
        entity.getId(),
        entity.getWorker(),
        entity.getStatus(),
        entity.getStartedAt(),
        entity.getEndedAt(),
        entity.getBackupFileId()
    );
  }
}