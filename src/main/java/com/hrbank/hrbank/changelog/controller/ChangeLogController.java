package com.hrbank.hrbank.changelog.controller;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogPageResponse;
import com.hrbank.hrbank.changelog.service.ChangeLogService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import com.hrbank.hrbank.changelog.entity.ChangeLog.ChangeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class ChangeLogController {
    private final ChangeLogService changeLogService;

    @InitBinder
    public void initBinder(WebDataBinder binder, HttpServletRequest servletRequest) {
        // 기존 API 이름과 프론트의 검색·페이지 이동 파라미터를 함께 받는다.
        binder.setNameResolver(parameter -> {
            String name = parameter.getParameterName();
            if ("atFrom".equals(name)) {
                return servletRequest.getParameter("atFrom") != null ? "atFrom" : "from";
            }
            if ("atTo".equals(name)) {
                return servletRequest.getParameter("atTo") != null ? "atTo" : "to";
            }
            if ("after".equals(name) && servletRequest.getParameter("after") == null) {
                String cursor = servletRequest.getParameter("cursor");
                if (cursor != null && !cursor.isBlank()) {
                    return "cursor";
                }
                String idAfter = servletRequest.getParameter("idAfter");
                if (idAfter != null && !"0".equals(idAfter)) {
                    return "idAfter";
                }
            }
            return name;
        });
        binder.registerCustomEditor(ChangeType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() || "ALL".equals(text)
                        ? null : ChangeType.valueOf(text));
            }
        });
        binder.registerCustomEditor(Sort.Direction.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(Sort.Direction.fromString(text));
            }
        });
    }

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
