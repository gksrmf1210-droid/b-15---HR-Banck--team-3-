package com.hrbank.hrbank.databackup.repository;

import com.hrbank.hrbank.databackup.BackupHistorySearchRequest;
import com.hrbank.hrbank.databackup.dto.BackupHistoryResponse;
import com.hrbank.hrbank.databackup.entity.BackupHistoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class BackupHistoryRepositoryImpl implements BackupHistoryRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public List<BackupHistoryResponse> searchHistories(BackupHistorySearchRequest request) {
    int size = (request.getSize() > 0) ? request.getSize() : 10;
    boolean isAsc = "ASC".equalsIgnoreCase(request.getDirection());
    String sortBy = "endedAt".equalsIgnoreCase(request.getSortBy()) ? "endedAt" : "startedAt";

    // 1. lastId가 전달된 경우, 기준이 되는 행의 시간값(targetTime) 조회
    LocalDateTime targetTime = findTargetTimeByLastId(request.getLastId(), sortBy);

    // 2. Criteria API 구성
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<BackupHistoryResponse> query = cb.createQuery(BackupHistoryResponse.class);
    Root<BackupHistoryEntity> root = query.from(BackupHistoryEntity.class);

    // Record 생성자에 맞춘 DTO 프로젝션 (모든 ID 및 파일 ID는 Long)
    query.select(cb.construct(
        BackupHistoryResponse.class,
        root.<Long>get("id"),
        root.get("worker"),
        root.get("status"),
        root.get("startedAt"),
        root.get("endedAt"),
        root.<Long>get("backupFileId")
    ));

    // WHERE 조건 조합
    List<Predicate> predicates = new ArrayList<>();

    // 2-1. worker 부분 일치 (LIKE %worker%)
    if (request.getWorker() != null && !request.getWorker().isBlank()) {
      predicates.add(cb.like(root.get("worker"), "%" + request.getWorker().trim() + "%"));
    }

    // 2-2. status 완전 일치
    if (request.getStatus() != null) {
      predicates.add(cb.equal(root.get("status"), request.getStatus()));
    }

    // 2-3. startedAt 기간 범위 조회
    if (request.getStartedAtFrom() != null && request.getStartedAtTo() != null) {
      predicates.add(cb.between(root.get("startedAt"), request.getStartedAtFrom(), request.getStartedAtTo()));
    } else if (request.getStartedAtFrom() != null) {
      predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), request.getStartedAtFrom()));
    } else if (request.getStartedAtTo() != null) {
      predicates.add(cb.lessThanOrEqualTo(root.get("startedAt"), request.getStartedAtTo()));
    }

    // 2-4. 복합 커서 조건: (시간 비교) OR (시간 동일 AND id 비교)
    if (request.getLastId() != null && targetTime != null) {
      Path<LocalDateTime> timePath = root.get(sortBy);
      Path<Long> idPath = root.get("id");

      Predicate cursorPredicate;
      if (isAsc) {
        cursorPredicate = cb.or(
            cb.greaterThan(timePath, targetTime),
            cb.and(cb.equal(timePath, targetTime), cb.greaterThan(idPath, request.getLastId()))
        );
      } else {
        cursorPredicate = cb.or(
            cb.lessThan(timePath, targetTime),
            cb.and(cb.equal(timePath, targetTime), cb.lessThan(idPath, request.getLastId()))
        );
      }
      predicates.add(cursorPredicate);
    }

    query.where(cb.and(predicates.toArray(new Predicate[0])));

    // 3. 1차 정렬(시간) + 2차 정렬(ID)
    if (isAsc) {
      query.orderBy(cb.asc(root.get(sortBy)), cb.asc(root.get("id")));
    } else {
      query.orderBy(cb.desc(root.get(sortBy)), cb.desc(root.get("id")));
    }

    // 4. size + 1 페이징 조회
    return em.createQuery(query)
        .setMaxResults(size + 1)
        .getResultList();
  }

  /**
   * 커서 기준 레코드(lastId)의 정렬 기준 시간 조회
   */
  private LocalDateTime findTargetTimeByLastId(Long lastId, String sortBy) {
    if (lastId == null) {
      return null;
    }

    String timeField = "endedAt".equalsIgnoreCase(sortBy) ? "b.endedAt" : "b.startedAt";
    String jpql = "SELECT " + timeField + " FROM BackupHistoryEntity b WHERE b.id = :lastId";

    List<LocalDateTime> result = em.createQuery(jpql, LocalDateTime.class)
        .setParameter("lastId", lastId)
        .setMaxResults(1)
        .getResultList();

    return result.isEmpty() ? null : result.get(0);
  }
}