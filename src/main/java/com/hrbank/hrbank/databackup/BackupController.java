package com.hrbank.hrbank.databackup;

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
  // TODO: BackupService 구현 후 주입하여 연결
  // private final BackupService backupService;

  /**
   * 백업 이력 목록 조회 API (Querydsl 커서 페이징 + 필터 검색)
   * GET /api/backups/histories
   */
  @GetMapping("/histories")
  public ResponseEntity<List<BackupHistoryResponse>> getHistories(
      @ModelAttribute BackupHistorySearchRequest request) {

    List<BackupHistoryResponse> response = historyRepository.searchHistories(request);
    return ResponseEntity.ok(response);
  }

  /**
   * 수동 데이터 백업 실행 API (작업자: 요청자 IP 주소)
   * POST /api/backups
   */
  @PostMapping
  public ResponseEntity<Void> triggerBackup(HttpServletRequest request) {
    String clientIp = extractClientIp(request);

    // backupService.executeBackup(clientIp);

    return ResponseEntity.accepted().build();
  }

  /**
   * 프록시, L4/L7 로드밸런서 환경을 고려한 클라이언트 IP 추출
   */
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