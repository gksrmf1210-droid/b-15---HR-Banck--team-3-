package com.hrbank.hrbank.databackup.service;

import com.hrbank.hrbank.databackup.BackupHistoryWriter;
import com.hrbank.hrbank.databackup.BackupStatus;
import com.hrbank.hrbank.databackup.entity.BackupHistoryEntity;
import com.hrbank.hrbank.databackup.repository.BackupHistoryRepository;
import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.file.entity.FileMeta;
import com.hrbank.hrbank.file.service.FileService;
import com.hrbank.hrbank.file.storage.FileStorage;
import jakarta.persistence.EntityManager;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

  private final BackupHistoryRepository historyRepository;
  private final BackupHistoryWriter historyWriter;
  private final EmployeeRepository employeeRepository;
  private final EntityManager entityManager;
  private final FileStorage fileStorage;

  private final FileService fileService;

  public void executeBackup(String worker) {
    // STEP 0. 이미 진행 중인 백업이 있는지 확인
    if (historyRepository.existsByStatus(BackupStatus.IN_PROGRESS)) {
      log.warn("이미 진행 중인 백업 작업이 존재합니다. (작업자: {})", worker);
      throw new IllegalStateException("이미 진행 중인 백업 작업이 있습니다.");
    }

    // STEP 1. 백업 필요 여부 판단
    // 백업 진행 중 변경된 데이터 유실 및 초 단위 오차 방지를 위해 startedAt 기준으로 비교
    LocalDateTime lastCompletedAt = historyRepository
        .findTopByStatusOrderByEndedAtDesc(BackupStatus.COMPLETED)
        .map(BackupHistoryEntity::getStartedAt)
        .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));

    // 초/밀리초 단위 버림 현상 대비: 1초 여유 버퍼 적용 (또는 existsByUpdatedAtGreaterThanEqual 사용)
    boolean hasChanged = employeeRepository.existsByUpdatedAtAfter(lastCompletedAt.minusSeconds(1));
    if (!hasChanged) {
      log.info("변경된 직원 데이터가 없어 백업을 건너뜁니다. (작업자: {})", worker);
      historyWriter.saveSkipped(worker);
      return;
    }

    // STEP 2. 백업 이력 등록 (IN_PROGRESS 상태 즉시 커밋)
    Long historyId = historyWriter.saveInProgress(worker);
    log.info("데이터 백업 프로세스 시작: historyId={}, worker={}", historyId, worker);

    Path tempCsvPath = null;
    Long uploadedBackupFileId = null;

    try {
      // STEP 3. 실제 백업 작업 수행 (OOM 방지 스트리밍 CSV 작성)
      tempCsvPath = Files.createTempFile("backup_employees_", ".csv");
      writeEmployeesToCsv(tempCsvPath);

      String fileName = "backup_employees_" + System.currentTimeMillis() + ".csv";
      long fileSize = Files.size(tempCsvPath);

      try (InputStream is = Files.newInputStream(tempCsvPath)) {
        // fileStorage.store -> fileService.upload 로 교체하여 FileMeta 엔티티 저장 및 Long ID 획득
        FileMeta backupFileMeta = fileService.upload(is, fileName, "text/csv", fileSize);
        uploadedBackupFileId = backupFileMeta.getId();
      }

      // STEP 4-1. 백업 성공 이력 갱신
      historyWriter.complete(historyId, uploadedBackupFileId);
      log.info("데이터 백업 프로세스 완료: historyId={}, fileId={}", historyId, uploadedBackupFileId);

    } catch (Exception e) {
      log.error("데이터 백업 프로세스 실패: historyId={}", historyId, e);

      // STEP 4-2. 실패 시 임시 파일 삭제 및 에러 로그 기록
      if (uploadedBackupFileId != null) {
        try {
          // FileService.delete(Long fileId) 호출 -> 물리 스토리지(UUID)와 DB 메타데이터 동시 정리
          fileService.delete(uploadedBackupFileId);
        } catch (Exception ignored) {}
      }

      Long errorLogFileId = storeErrorLog(e);
      historyWriter.fail(historyId, errorLogFileId);

    } finally {
      if (tempCsvPath != null) {
        try {
          Files.deleteIfExists(tempCsvPath);
        } catch (Exception ignored) {}
      }
    }
  }

  private void writeEmployeesToCsv(Path filePath) throws Exception {
    try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
      writer.write("id,employeeNumber,name,email,departmentName,position,status,hireDate\n");

      Long lastId = 0L;
      int chunkSize = 500;

      while (true) {
        List<Employee> batch = employeeRepository.findEmployeesChunk(lastId, PageRequest.of(0, chunkSize));
        if (batch.isEmpty()) {
          break;
        }

        for (Employee emp : batch) {
          String deptName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "";

          writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
              emp.getId(),
              escapeCsv(emp.getEmployeeNumber()),
              escapeCsv(emp.getName()),
              escapeCsv(emp.getEmail()),
              escapeCsv(deptName),
              escapeCsv(emp.getPosition() != null ? emp.getPosition() : ""),
              escapeCsv(emp.getStatus() != null ? emp.getStatus().name() : ""),
              emp.getHireDate() != null ? emp.getHireDate().toString() : ""
          ));
          lastId = emp.getId();
        }

        writer.flush();
        entityManager.clear();
      }
    }
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  /**
   * 에러 로그 .log 파일 생성 및 업로드 (반환 타입: Long)
   */
  private Long storeErrorLog(Exception e) {
    Path tempLogPath = null;
    try {
      tempLogPath = Files.createTempFile("backup_error_", ".log");

      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      Files.writeString(tempLogPath, sw.toString(), StandardCharsets.UTF_8);

      String errorLogName = "backup_error_" + System.currentTimeMillis() + ".log";
      long fileSize = Files.size(tempLogPath);

      try (InputStream is = Files.newInputStream(tempLogPath)) {
        // FileService의 upload 호출 후 Long 타입 PK 획득
        FileMeta errorFileMeta = fileService.upload(is, errorLogName, "text/plain", fileSize);
        return errorFileMeta.getId();
      }
    } catch (Exception ex) {
      log.error("에러 로그 파일 생성 및 저장 중 오류 발생", ex);
      return null;
    } finally {
      if (tempLogPath != null) {
        try {
          Files.deleteIfExists(tempLogPath);
        } catch (Exception ignored) {}
      }
    }
  }
    }
