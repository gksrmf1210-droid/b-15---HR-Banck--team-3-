package com.hrbank.hrbank.databackup;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class BackupHistorySearchRequest {


  //필터 조건
  private String worker;
  private BackupStatus status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime startedAtFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime startedAtTo;

  // 정렬 조건 (startedAt 또는 endedAt, 기본값 startedAt)
  private String sortBy = "startedAt";
  private String direction = "DESC";

  private Long lastId;

  // 페이지 크기
  private int size = 10;

}
