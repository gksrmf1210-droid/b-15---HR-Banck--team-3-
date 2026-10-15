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
            cursorCondition(request.getLastId()) // 이전 페이지 마지막 ID 기반 No-offset
        )
        .orderBy(createOrderSpecifier(request.getSortBy(), request.getDirection()))
        .limit(request.getSize())
        .fetch();
  }

  // 1. 작업자 부분 일치
  private BooleanExpression workerContains(String worker) {
    return (worker != null && !worker.isBlank())
        ? backupHistoryEntity.worker.contains(worker.trim())
        : null;
  }

  // 2. 상태 완전 일치
  private BooleanExpression statusEq(BackupStatus status) {
    return status != null ? backupHistoryEntity.status.eq(status) : null;
  }

  // 3. 시작 시간 범위 조건
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

  // 4. No-offset 커서 페이징 (기본 최신순 내림차순: id < lastId)
  private BooleanExpression cursorCondition(Long lastId) {
    return lastId != null ? backupHistoryEntity.id.lt(lastId) : null;
  }

  // 5. 단일 정렬 조건 (startedAt 또는 endedAt)
  private OrderSpecifier<?> createOrderSpecifier(String sortBy, String direction) {
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    if ("endedAt".equalsIgnoreCase(sortBy)) {
      return isAsc ? backupHistoryEntity.endedAt.asc() : backupHistoryEntity.endedAt.desc();
    }
    // 기본값: startedAt
    return isAsc ? backupHistoryEntity.startedAt.asc() : backupHistoryEntity.startedAt.desc();
  }
}