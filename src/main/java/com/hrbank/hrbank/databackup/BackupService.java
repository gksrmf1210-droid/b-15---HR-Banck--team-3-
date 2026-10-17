package com.hrbank.hrbank.databackup;

import com.hrbank.hrbank.employee.entity.Employee;
import com.hrbank.hrbank.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
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
    try {
      // -------------------------------------------------------------------------
      // STEP.3 실제 백업 작업 수행 (OOM 방지 스트리밍 CSV 작성)
      // -------------------------------------------------------------------------
      tempCsvPath = Files.createTempFile("backup_employees_", ".csv");
      writeEmployeesToCsv(tempCsvPath);

      // TODO: 파일 관리 팀원의 파일 저장 서비스 호출 (임시 파일 저장 후 fileId 획득)
      Long backupFileId = 1L;

      // -------------------------------------------------------------------------
      // STEP.4-1 성공 처리 (COMPLETED 갱신 및 파일 정보 매핑)
      // -------------------------------------------------------------------------
      historyWriter.complete(historyId, backupFileId);
      log.info("데이터 백업 프로세스 완료: historyId={}", historyId);

    } catch (Exception e) {
      log.error("데이터 백업 프로세스 실패: historyId={}", historyId, e);

      // STEP.4-2 실패 시 작성 중이던 임시 CSV 파일 삭제
      if (tempCsvPath != null) {
        try {
          Files.deleteIfExists(tempCsvPath);
        } catch (Exception ignored) {}
      }

      // STEP.4-2 에러 로그를 .log 파일로 생성 및 실패 처리
      Long errorLogFileId = storeErrorLog(e);
      historyWriter.fail(historyId, errorLogFileId);
    } finally {
      // 업로드 성공 후에도 로컬 임시 CSV 파일 정리
      if (tempCsvPath != null) {
        try {
          Files.deleteIfExists(tempCsvPath);
        } catch (Exception ignored) {}
      }
    }
  }

  /**
   * OOM 방지: 커서(ID) 기반으로 500건씩 분할 조회하여 파일 스트림에 즉시 기록하고
   * JPA 1차 캐시를 비워 힙 메모리 점유를 방지함
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
              emp.getEmployeeNumber(),
              emp.getName(),
              emp.getEmail(),
              deptName,
              emp.getPosition(),
              emp.getStatus(),
              emp.getHireDate()
          ));
          lastId = emp.getId();
        }
        writer.flush();         // 버퍼 비우기 (디스크 기록)
        entityManager.clear();  // ★ JPA 1차 캐시 초기화 (OOM 방지 핵심)
      }
    }
  }

  /**
   * 에러 스택트레이스를 .log 파일로 생성
   */
  private Long storeErrorLog(Exception e) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      e.printStackTrace(new PrintStream(out));

      Path tempLogPath = Files.createTempFile("backup_error_", ".log");
      Files.write(tempLogPath, out.toByteArray());

      // TODO: 파일 관리 팀원의 파일 저장 서비스 호출 후 fileId 획득
      Long errorLogFileId = 999L;
      Files.deleteIfExists(tempLogPath);
      return errorLogFileId;
    } catch (Exception ex) {
      log.error("에러 로그 파일 생성 중 추가 예외 발생", ex);
      return null;
    }
  }
}