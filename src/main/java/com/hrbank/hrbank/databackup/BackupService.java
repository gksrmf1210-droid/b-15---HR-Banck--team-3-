package com.hrbank.hrbank.databackup;

import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.storage.FileStorage;
import jakarta.persistence.EntityManager;
import java.io.BufferedWriter;
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

  public void executeBackup(String worker) {
    // -------------------------------------------------------------------------
    // STEP.0 이미 진행 중인 백업이 있는지 확인 (수동 백업 시 409 발생용)
    // -------------------------------------------------------------------------
    if (historyRepository.existsByStatus(BackupStatus.IN_PROGRESS)) {
      log.warn("이미 진행 중인 백업 작업이 존재합니다. (작업자: {})", worker);
      throw new IllegalStateException("이미 진행 중인 백업 작업이 있습니다.");
    }

    // -------------------------------------------------------------------------
    // STEP.1 백업 필요 여부 판단
    // -------------------------------------------------------------------------
    LocalDateTime lastCompletedAt = historyRepository
        .findTopByStatusOrderByEndedAtDesc(BackupStatus.COMPLETED)
        .map(BackupHistoryEntity::getEndedAt)
        .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));

    // 최근 완료 시간 이후 변경된 직원이 없으면 건너뜀 처리 후 프로세스 종료
    boolean hasChanged = employeeRepository.existsByUpdatedAtAfter(lastCompletedAt);
    if (!hasChanged) {
      log.info("변경된 직원 데이터가 없어 백업을 건너뜁니다. (작업자: {})", worker);
      historyWriter.saveSkipped(worker);
      return;
    }

    // -------------------------------------------------------------------------
    // STEP.2 백업 이력 등록 (IN_PROGRESS 상태 즉시 커밋)
    // -------------------------------------------------------------------------
    Long historyId = historyWriter.saveInProgress(worker);
    log.info("데이터 백업 프로세스 시작: historyId={}, worker={}", historyId, worker);

    Path tempCsvPath = null;
    UUID uploadedBackupFileId = null;

    try {
      // -------------------------------------------------------------------------
      // STEP.3 실제 백업 작업 수행 (OOM 방지 스트리밍 CSV 작성)
      // -------------------------------------------------------------------------
      tempCsvPath = Files.createTempFile("backup_employees_", ".csv");
      writeEmployeesToCsv(tempCsvPath);

      // FileStorage 시그니처(InputStream, fileName)에 맞춰 호출
      String fileName = "backup_employees_" + System.currentTimeMillis() + ".csv";
      try (InputStream is = Files.newInputStream(tempCsvPath)) {
        uploadedBackupFileId = fileStorage.store(is, fileName);
      }

      // STEP.4-1 백업 성공 이력 갱신
      historyWriter.complete(historyId, uploadedBackupFileId);
      log.info("데이터 백업 프로세스 완료: historyId={}, fileId={}", historyId, uploadedBackupFileId);

    } catch (Exception e) {
      log.error("데이터 백업 프로세스 실패: historyId={}", historyId, e);

      // STEP.4-2 실패 시 STEP.3에서 저장하던 파일 삭제
      if (uploadedBackupFileId != null) {
        try {
          fileStorage.delete(uploadedBackupFileId);
        } catch (Exception ignored) {}
      }

      // STEP.4-2 에러 로그를 .log 파일로 생성 및 실패 처리
      UUID errorLogFileId = storeErrorLog(e);
      historyWriter.fail(historyId, errorLogFileId);

    } finally {
      if (tempCsvPath != null) {
        try {
          Files.deleteIfExists(tempCsvPath);
        } catch (Exception ignored) {}
      }
    }
  }

  /**
   * OOM 방지: 커서(ID) 기반으로 500건씩 분할 조회하여 파일 스트림에 즉시 기록
   */
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

          // position은 String이므로 .name()을 제거하고 바로 전달
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
   * 발생한 예외의 스택 트레이스를 임시 .log 파일로 저장하고 FileStorage에 업로드
   */
  private UUID storeErrorLog(Exception e) {
    Path tempLogPath = null;
    try {
      tempLogPath = Files.createTempFile("backup_error_", ".log");

      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      Files.writeString(tempLogPath, sw.toString(), StandardCharsets.UTF_8);

      String errorLogName = "backup_error_" + System.currentTimeMillis() + ".log";
      try (InputStream is = Files.newInputStream(tempLogPath)) {
        return fileStorage.store(is, errorLogName);
      }
    } catch (Exception ex) {
      log.error("에러 로그 파일 생성/저장 실패", ex);
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