package com.hrbank.hrbank.databackup;

import com.hrbank.hrbank.databackup.entity.BackupHistoryEntity;
import com.hrbank.hrbank.databackup.repository.BackupHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BackupHistoryWriter {

  private final BackupHistoryRepository historyRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BackupHistoryEntity saveSkipped(String worker) {
    return historyRepository.save(BackupHistoryEntity.skip(worker));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Long saveInProgress(String worker) {
    BackupHistoryEntity history = BackupHistoryEntity.start(worker);
    return historyRepository.save(history).getId();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void complete(Long historyId, Long backupFileId) {
    historyRepository.findById(historyId).ifPresent(history -> {
      history.complete(backupFileId);
    });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void fail(Long historyId, Long errorLogFileId) {
    historyRepository.findById(historyId).ifPresent(history -> {
      history.fail(errorLogFileId);
    });
  }
}