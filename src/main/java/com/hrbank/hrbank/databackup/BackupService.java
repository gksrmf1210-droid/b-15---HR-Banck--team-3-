package com.hrbank.hrbank.databackup;

import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.storage.FileStorage;
import jakarta.persistence.EntityManager;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    // STEP.1 백업 필요 여부 판단
    LocalDateTime lastCompletedAt = historyRepository
        .findTopByStatusOrderByEndedAtDesc(BackupStatus.COMPLETED)
        .map(BackupHistoryEntity::getEndedAt)
        .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));

    boolean hasChanged = employeeRepository.existsByUpdatedAtAfter(lastCompletedAt);
    if (!hasChanged) {
      log.info("변경된 직원 데이터가 없어 백업을 건너뜁니다. (작업자: {})", worker);
      historyWriter.saveSkipped(worker);
      return;
    }

    // STEP.2 백업 이력 등록 (IN_PROGRESS 상태 즉시 커밋)
    Long historyId = historyWriter.saveInProgress(worker);
    log.info("데이터 백업 프로세스 시작: historyId={}, worker={}", historyId, worker);

    Path tempCsvPath = null;
    UUID uploadedBackupFileId = null;

    try {
      // STEP.3 실제 백업 작업 수행 (OOM 방지 스트리밍 CSV 작성)
      tempCsvPath = Files.createTempFile("backup_employees_", ".csv");
      writeEmployeesToCsv(tempCsvPath);

      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      String fileName = String.format("backup_employees_%s.csv", timestamp);

      try (InputStream in = Files.newInputStream(tempCsvPath)) {
        uploadedBackupFileId = fileStorage.store(in, fileName);
      }

      // STEP.4-1 성공 처리
      historyWriter.complete(historyId, uploadedBackupFileId);
      log.info("데이터 백업 프로세스 완료: historyId={}, fileId={}", historyId, uploadedBackupFileId);

    } catch (Exception e) {
      log.error("데이터 백업 프로세스 실패: historyId={}", historyId, e);

      // STEP.4-2 실패 시 STEP.3에서 저장하던 파일 삭제
      if (uploadedBackupFileId != null) {
        try {
          fileStorage.delete(uploadedBackupFileId);
          log.info("실패로 인한 백업 파일 삭제 완료: fileId={}", uploadedBackupFileId);
        } catch (Exception deleteEx) {
          log.warn("실패한 백업 파일 삭제 중 오류 발생: fileId={}", uploadedBackupFileId, deleteEx);
        }
      }

      // STEP.4-2 에러 로그를 .log 파일로 저장 및 실패 처리
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
              escapeCsv(emp.getPosition() != null ? emp.getPosition().toString() : ""),
              escapeCsv(emp.getStatus() != null ? emp.getStatus().toString() : ""),
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

  private UUID storeErrorLog(Exception e) {
    try {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      byte[] logBytes = sw.toString().getBytes(StandardCharsets.UTF_8);

      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      String logFileName = String.format("backup_error_%s.log", timestamp);

      try (InputStream in = new ByteArrayInputStream(logBytes)) {
        return fileStorage.store(in, logFileName);
      }
    } catch (Exception ex) {
      log.error("에러 로그 파일 스토리지 저장 중 추가 오류 발생", ex);
      return null;
    }
  }
}