package com.hrbank.hrbank.databackup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

  // TODO: BackupService 구현 후 주입
  // private final BackupService backupService;

  /**
   * 1시간(기본 3,600,000ms)마다 백업 자동 실행
   * application.yml의 backup.schedule.rate 설정으로 주기 변경 가능
   */
  @Scheduled(fixedRateString = "${backup.schedule.rate:3600000}")
  public void runScheduledBackup() {
    log.info("정기 데이터 백업 스케줄러 실행 (작업자: system)");

    // backupService.executeBackup("system");
  }
}