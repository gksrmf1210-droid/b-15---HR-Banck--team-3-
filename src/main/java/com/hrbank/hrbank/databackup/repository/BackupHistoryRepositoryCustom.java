package com.hrbank.hrbank.databackup.repository;

import com.hrbank.hrbank.databackup.dto.BackupHistoryResponse;
import com.hrbank.hrbank.databackup.BackupHistorySearchRequest;
import java.util.List;

public interface BackupHistoryRepositoryCustom {
  List<BackupHistoryResponse> searchHistories(BackupHistorySearchRequest request);
}
