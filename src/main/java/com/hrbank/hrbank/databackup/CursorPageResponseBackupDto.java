package com.hrbank.hrbank.databackup;

import java.util.List;

public record CursorPageResponseBackupDto(
    List<BackupHistoryResponse> content,
    Long nextCursor,
    boolean hasNext
) {
  public static CursorPageResponseBackupDto of(List<BackupHistoryResponse> items, int requestedSize) {
    boolean hasNext = items.size() > requestedSize;
    List<BackupHistoryResponse> pagedContent = hasNext ? items.subList(0, requestedSize) : items;
    Long nextCursor = pagedContent.isEmpty() ? null : pagedContent.get(pagedContent.size() - 1).id();

    return new CursorPageResponseBackupDto(pagedContent, nextCursor, hasNext);
  }
}