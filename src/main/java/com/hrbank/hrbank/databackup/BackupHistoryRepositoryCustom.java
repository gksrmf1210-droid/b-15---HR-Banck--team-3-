package com.hrbank.hrbank.databackup;

import java.util.List;

public interface BackupHistoryRepositoryCustom {
  List<BackupHistoryResponse> searchHistories(BackupHistorySearchRequest request);
}
