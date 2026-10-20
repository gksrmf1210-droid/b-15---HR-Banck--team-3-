package com.hrbank.hrbank.databackup;

import com.hrbank.hrbank.databackup.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

  private final BackupService backupService;

  @Scheduled(fixedRateString = "${backup.schedule.rate:3600000}")
  public void runScheduledBackup() {
    log.info("정기 데이터 백업 스케줄러 실행 (작업자: system)");
    backupService.executeBackup("system");
  }
}