package com.hrbank.hrbank.changelog.controller;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogPageResponse;
import com.hrbank.hrbank.changelog.service.ChangeLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class ChangeLogController {
    private final ChangeLogService changeLogService;

    // 직원 정보 수정 이력 목록
    @GetMapping
    public ChangeLogPageResponse search(@Valid @ModelAttribute ChangeLogSearchRequest request) {
        return changeLogService.search(request);
    }

    // 이력 상세 변경 내용 조회
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getChangeLogDetail(@PathVariable Long id) {
        Map<String, Object> response = changeLogService.getId(id);
        return ResponseEntity.ok(response);
    }

    // 최근 일주일 수정 이력 건수
    @GetMapping("/count")
    public ResponseEntity<Long> getChangeLogsCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        Long count = changeLogService.getChangeLogsCount(fromDate, toDate);
        return ResponseEntity.ok(count);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleInvalidCursor() {
        return ResponseEntity.badRequest().build();
    }


}
