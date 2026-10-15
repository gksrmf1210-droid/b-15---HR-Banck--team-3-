package com.hrbank.hrbank.databackup;

import java.time.LocalDateTime;

public record BackupHistoryResponse(
    Long id,
    String worker,
    BackupStatus status,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    Long backupFileId
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