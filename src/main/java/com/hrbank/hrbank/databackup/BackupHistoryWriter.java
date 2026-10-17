package com.hrbank.hrbank.databackup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


// 스프링의 @Transactional은 AOP 프록시(Proxy) 방식으로 동작합니다.
// 같은 클래스 내부에서 this.saveInProgress()를 호출하면 스프링의 AOP 프록시를
// 거치지 않고 자바 순수 메서드가 직접 호출되기 때문에,
// @Transactional 설정이 아예 무시되어 버립니다 (Self-invocation 문제).
// 그래서 스프링 컨테이너가 프록시로 트랜잭션을 정상 가로챌 수 있도록
// BackupHistoryWriter라는 별도의 독립된 스프링 빈(Bean)으로 추출한 것

@Component
@RequiredArgsConstructor
public class BackupHistoryWriter {

  private final BackupHistoryRepository historyRepository;

  /**
   * STEP.1: 변경 데이터가 없을 때 건너뜀(SKIPPED) 상태 즉시 저장
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BackupHistoryEntity saveSkipped(String worker) {
    return historyRepository.save(BackupHistoryEntity.skip(worker));
  }

  /**
   * STEP.2: 진행중(IN_PROGRESS) 상태를 즉시 커밋하여 STEP.3 도중 외부 조회 가능하도록 함
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Long saveInProgress(String worker) {
    BackupHistoryEntity history = BackupHistoryEntity.start(worker);
    return historyRepository.save(history).getId();
  }

  /**
   * STEP.4-1: 성공 시 완료(COMPLETED) 상태로 갱신
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void complete(Long historyId, Long backupFileId) {
    historyRepository.findById(historyId).ifPresent(history -> {
      history.complete(backupFileId);
    });
  }

  /**
   * STEP.4-2: 실패 시 실패(FAILED) 상태 및 에러 로그 파일 ID 갱신
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void fail(Long historyId, Long errorLogFileId) {
    historyRepository.findById(historyId).ifPresent(history -> {
      history.fail(errorLogFileId);
    });
  }
}