package com.hrbank.hrbank.databackup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backup_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BackupHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // PK는 Long 유지

  @Column(nullable = false)
  private String worker;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BackupStatus status;

  @Column(nullable = false)
  private LocalDateTime startedAt;

  private LocalDateTime endedAt;

  // 파일 ID만 UUID로 변경
  @Column(columnDefinition = "BINARY(16)") // 또는 String 변환을 원하시면 varchar(36)
  private UUID backupFileId;

  public static BackupHistoryEntity start(String worker) {
    BackupHistoryEntity history = new BackupHistoryEntity();
    history.worker = worker;
    history.status = BackupStatus.IN_PROGRESS;
    history.startedAt = LocalDateTime.now();
    return history;
  }

  public static BackupHistoryEntity skip(String worker) {
    BackupHistoryEntity history = new BackupHistoryEntity();
    history.worker = worker;
    history.status = BackupStatus.SKIPPED;
    LocalDateTime now = LocalDateTime.now();
    history.startedAt = now;
    history.endedAt = now;
    return history;
  }

  // 파라미터 타입을 UUID로 수정
  public void complete(UUID backupFileId) {
    this.status = BackupStatus.COMPLETED;
    this.endedAt = LocalDateTime.now();
    this.backupFileId = backupFileId;
  }

  // 파라미터 타입을 UUID로 수정
  public void fail(UUID errorLogFileId) {
    this.status = BackupStatus.FAILED;
    this.endedAt = LocalDateTime.now();
    this.backupFileId = errorLogFileId;
  }
}