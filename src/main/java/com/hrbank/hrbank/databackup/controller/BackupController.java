package com.hrbank.hrbank.databackup.controller;

import com.hrbank.hrbank.databackup.repository.BackupHistoryRepository;
import com.hrbank.hrbank.databackup.dto.BackupHistoryResponse;
import com.hrbank.hrbank.databackup.BackupHistorySearchRequest;
import com.hrbank.hrbank.databackup.service.BackupService;
import com.hrbank.hrbank.databackup.dto.CursorPageResponseBackupDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backups")
public class BackupController {

  private final BackupHistoryRepository historyRepository;
  private final BackupService backupService;

// 1. 데이터 백업 목록 조회 API (Querydsl 커서 페이징 + 필터 검색)

  @GetMapping
  public ResponseEntity<CursorPageResponseBackupDto> getBackups(
      @ModelAttribute BackupHistorySearchRequest request) {

    // null 체크를 빼고 0 초과 여부만 확인
    int size = (request.getSize() > 0) ? request.getSize() : 10;
    List<BackupHistoryResponse> results = historyRepository.searchHistories(request);

    return ResponseEntity.ok(CursorPageResponseBackupDto.of(results, size));
  }

 //2. 수동 데이터 백업 실행 API (작업자: 요청자 IP 주소)

  @PostMapping
  public ResponseEntity<Void> triggerBackup(HttpServletRequest request) {
    String clientIp = extractClientIp(request);
    backupService.executeBackup(clientIp);
    return ResponseEntity.accepted().build();
  }


  //3. 최근 백업 정보 조회 API (대시보드 위젯 연동)

  @GetMapping("/latest")
  public ResponseEntity<BackupHistoryResponse> getLatestBackup() {
    return historyRepository.findTopByOrderByIdDesc()
        .map(BackupHistoryResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
  }

// 프록시, L4/L7 로드밸런서 환경을 고려한 클라이언트 IP 추출

  private String extractClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    } else {
      // 프록시를 여러 개 거쳐 IP 목록이 콤마로 연결된 경우 첫 번째가 실제 클라이언트 IP
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}