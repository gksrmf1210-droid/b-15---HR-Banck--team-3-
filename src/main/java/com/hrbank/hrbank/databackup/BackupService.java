package com.hrbank.hrbank.databackup;

import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import com.hrbank.hrbank.storage.FileStorage;
import jakarta.persistence.EntityManager;
import java.io.BufferedWriter;
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



    } catch (Exception e) {
      log.error("데이터 백업 프로세스 실패: historyId={}", historyId, e);

      if (tempCsvPath != null) {
        try {
          Files.deleteIfExists(tempCsvPath);
        } catch (Exception ignored) {}
      }

      // STEP.4-2 에러 로그를 .log 파일로 생성 및 실패 처리
      Long errorLogFileId = storeErrorLog(e);
      historyWriter.fail(historyId, errorLogFileId);
    }
  }

  /**
   * OOM 방지: 커서(ID) 기반으로 500건씩 분할 조회하여 파일 스트림에 즉시 기록
   */
  private void writeEmployeesToCsv(Path filePath) throws Exception {
    try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
      // CSV 헤더 작성 (팀원의 Employee 엔티티 필드 기준)
      writer.write("id,employeeNumber,name,email,departmentName,position,status,hireDate\n");

      Long lastId = 0L;
      int chunkSize = 500;

      while (true) {
        // 커서(ID > lastId) 기반 청크 조회
        List<Employee> batch = employeeRepository.findEmployeesChunk(lastId, PageRequest.of(0, chunkSize));
        if (batch.isEmpty()) {
          break;
        }

        for (Employee emp : batch) {
          String deptName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "";
          writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
              emp.getId(),
          ));
          lastId = emp.getId();
        }
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

    try {


    } catch (Exception ex) {
      return null;
    }
  }
}