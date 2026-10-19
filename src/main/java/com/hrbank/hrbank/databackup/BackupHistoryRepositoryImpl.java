package com.hrbank.hrbank.databackup;

import static com.hrbank.hrbank.databackup.QBackupHistoryEntity.backupHistoryEntity;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BackupHistoryRepositoryImpl implements BackupHistoryRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<BackupHistoryResponse> searchHistories(BackupHistorySearchRequest request) {
    int size = (request.getSize() > 0) ? request.getSize() : 10;
    boolean isAsc = "ASC".equalsIgnoreCase(request.getDirection());
    String sortBy = "endedAt".equalsIgnoreCase(request.getSortBy()) ? "endedAt" : "startedAt";

    // 1. lastId가 전달된 경우, 기준이 되는 행의 시간값을 조회
    LocalDateTime targetTime = findTargetTimeByLastId(request.getLastId(), sortBy);

    return queryFactory
        .select(Projections.constructor(
            BackupHistoryResponse.class,
            backupHistoryEntity.id,
            backupHistoryEntity.worker,
            backupHistoryEntity.status,
            backupHistoryEntity.startedAt,
            backupHistoryEntity.endedAt,
            backupHistoryEntity.backupFileId
        ))
        .from(backupHistoryEntity)
        .where(
            workerContains(request.getWorker()),
            statusEq(request.getStatus()),
            startedAtBetween(request.getStartedAtFrom(), request.getStartedAtTo()),
            // 2. 시간 + ID 복합 커서 조건 적용
            cursorCondition(request.getLastId(), targetTime, sortBy, isAsc)
        )
        // 3. 동일 시간 데이터에 대비해 id를 2차 정렬 조건으로 추가
        .orderBy(createOrderSpecifiers(sortBy, isAsc))
        .limit(size + 1)
        .fetch();
  }

  /**
   * 커서 기준 레코드(lastId)의 정렬 기준 시간 조회
   */
  private LocalDateTime findTargetTimeByLastId(Long lastId, String sortBy) {
    if (lastId == null) {
      return null;
    }
    return queryFactory
        .select("endedAt".equals(sortBy) ? backupHistoryEntity.endedAt : backupHistoryEntity.startedAt)
        .from(backupHistoryEntity)
        .where(backupHistoryEntity.id.eq(lastId))
        .fetchOne();
  }

  /**
   * 복합 커서 조건 생성
   * - 내림차순(DESC): (시간 < targetTime) OR (시간 == targetTime AND id < lastId)
   * - 오름차순(ASC) : (시간 > targetTime) OR (시간 == targetTime AND id > lastId)
   */
  private BooleanExpression cursorCondition(Long lastId, LocalDateTime targetTime, String sortBy, boolean isAsc) {
    if (lastId == null || targetTime == null) {
      return null;
    }

    var timePath = "endedAt".equals(sortBy) ? backupHistoryEntity.endedAt : backupHistoryEntity.startedAt;

    if (isAsc) {
      return timePath.gt(targetTime)
          .or(timePath.eq(targetTime).and(backupHistoryEntity.id.gt(lastId)));
    } else {
      return timePath.lt(targetTime)
          .or(timePath.eq(targetTime).and(backupHistoryEntity.id.lt(lastId)));
    }
  }

  /**
   * 1차 정렬(시간) + 2차 정렬(ID)로 정렬 순서 고정
   */
  private OrderSpecifier<?>[] createOrderSpecifiers(String sortBy, boolean isAsc) {
    var timePath = "endedAt".equalsIgnoreCase(sortBy) ? backupHistoryEntity.endedAt : backupHistoryEntity.startedAt;

    return new OrderSpecifier<?>[] {
        isAsc ? timePath.asc() : timePath.desc(),
        isAsc ? backupHistoryEntity.id.asc() : backupHistoryEntity.id.desc()
    };
  }

  private BooleanExpression workerContains(String worker) {
    return (worker != null && !worker.isBlank())
        ? backupHistoryEntity.worker.contains(worker.trim())
        : null;
  }

  private BooleanExpression statusEq(BackupStatus status) {
    return status != null ? backupHistoryEntity.status.eq(status) : null;
  }

  private BooleanExpression startedAtBetween(LocalDateTime from, LocalDateTime to) {
    if (from != null && to != null) {
      return backupHistoryEntity.startedAt.between(from, to);
    } else if (from != null) {
      return backupHistoryEntity.startedAt.goe(from);
    } else if (to != null) {
      return backupHistoryEntity.startedAt.loe(to);
    }
    return null;
  }
}