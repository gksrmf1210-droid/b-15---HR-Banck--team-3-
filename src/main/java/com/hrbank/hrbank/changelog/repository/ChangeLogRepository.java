package com.hrbank.hrbank.changelog.repository;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogResponse;
import com.hrbank.hrbank.changelog.entity.ChangeLog;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.Instant;

import java.util.List;
import java.util.NoSuchElementException;


public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

    // 이력 개수 세기
    long countByCreatedAtBetween(Instant from, Instant to);


    // 화면에 보여줄 모양으로 바로 꺼내기 -> (ChangeLogResponse)
    String SELECT = "SELECT new com.hrbank.hrbank.changelog.dto.response.ChangeLogResponse("
            + "c.id, c.employeeNumber, c.memo, c.ipAddress, c.createdAt, c.type) "
            + "FROM ChangeLog c WHERE ";

    // 검색 조건: 값이 null이면 그 조건은 통과, 값이 있으면 비교. 전부 AND
    String FILTER = """
                (:employeeNumber IS NULL OR c.employeeNumber LIKE CONCAT('%', :employeeNumber, '%') ESCAPE '\\')
            AND (:memo IS NULL OR c.memo LIKE CONCAT('%', :memo, '%') ESCAPE '\\')
            AND (:ipAddress IS NULL OR c.ipAddress LIKE CONCAT('%', :ipAddress, '%') ESCAPE '\\')
            AND (:type IS NULL OR c.type = :type)
            AND (:from IS NULL OR c.createdAt >= :from)
            AND (:to IS NULL OR c.createdAt <= :to)
            """;

    // ==== 시간순 정렬 ====
    // 최신순 (내림차순)
    @Query(SELECT + FILTER + """
            AND (:lastId IS NULL OR c.createdAt < :lastCreatedAt
                 OR (c.createdAt = :lastCreatedAt AND c.id < :lastId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<ChangeLogResponse> searchByTimeDesc(
            @Param("employeeNumber") String employeeNumber, @Param("memo") String memo,
            @Param("ipAddress") String ipAddress, @Param("type") ChangeLog.ChangeType type,
            @Param("from") Instant from, @Param("to") Instant to,
            @Param("lastId") Long lastId, @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable);

    // 오래된순 (오름차순)
    @Query(SELECT + FILTER + """
            AND (:lastId IS NULL OR c.createdAt > :lastCreatedAt
                 OR (c.createdAt = :lastCreatedAt AND c.id > :lastId))
            ORDER BY c.createdAt ASC, c.id ASC
            """)
    List<ChangeLogResponse> searchByTimeAsc(
            @Param("employeeNumber") String employeeNumber, @Param("memo") String memo,
            @Param("ipAddress") String ipAddress, @Param("type") ChangeLog.ChangeType type,
            @Param("from") Instant from, @Param("to") Instant to,
            @Param("lastId") Long lastId, @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable);

    // ===== IP순 정렬 =====
    // IP 내림차순
    @Query(SELECT + FILTER + """
            AND (:lastId IS NULL
                 OR (:lastIp IS NOT NULL AND (c.ipAddress < :lastIp
                      OR (c.ipAddress = :lastIp AND c.id < :lastId)
                      OR c.ipAddress IS NULL))
                 OR (:lastIp IS NULL AND c.ipAddress IS NULL AND c.id < :lastId))
            ORDER BY c.ipAddress DESC NULLS LAST, c.id DESC
            """)
    List<ChangeLogResponse> searchByIpDesc(
            @Param("employeeNumber") String employeeNumber, @Param("memo") String memo,
            @Param("ipAddress") String ipAddress, @Param("type") ChangeLog.ChangeType type,
            @Param("from") Instant from, @Param("to") Instant to,
            @Param("lastId") Long lastId, @Param("lastIp") String lastIp,
            Pageable pageable);

    // IP 오름차순
    @Query(SELECT + FILTER + """
            AND (:lastId IS NULL
                 OR (:lastIp IS NOT NULL AND (c.ipAddress > :lastIp
                      OR (c.ipAddress = :lastIp AND c.id > :lastId)
                      OR c.ipAddress IS NULL))
                 OR (:lastIp IS NULL AND c.ipAddress IS NULL AND c.id > :lastId))
            ORDER BY c.ipAddress ASC NULLS LAST, c.id ASC
            """)
    List<ChangeLogResponse> searchByIpAsc(
            @Param("employeeNumber") String employeeNumber, @Param("memo") String memo,
            @Param("ipAddress") String ipAddress, @Param("type") ChangeLog.ChangeType type,
            @Param("from") Instant from, @Param("to") Instant to,
            @Param("lastId") Long lastId, @Param("lastIp") String lastIp,
            Pageable pageable);



    // 이력 목록 검색
    // 사용자가 입력한 조건(사번, 메모, IP, 시간, 유형)에 맞는 이력만 골라서,원하는 순서(IP,시간)로 한 페이지만큼 가져온다.
    default List<ChangeLogResponse> search(ChangeLogSearchRequest request) {

        // 지난 페이지의 마지막 이력(책갈피). request.after()는 그 이력의 번호(ID)
        //   - 번호가 없으면 → 첫 페이지라서 책갈피 없음(null)
        //   - 번호가 있으면 → DB에서 찾아서 IP, 시간 같은 정보를 꺼내 둔다
        //   - 찾아봤는데 없으면 → 에러 (이미 지워졌거나 잘못된 번호)
        ChangeLog last = request.after() == null ? null
                : findById(request.after())
                .orElseThrow(() -> new NoSuchElementException("이전 페이지의 마지막 이력을 찾을 수 없습니다."));
        Long lastId = last == null ? null : last.getId();

        // 검색어 비어 있으면 null(조건 무시), %, _ 는 글자로 취급되게 이스케이프
        String employeeNumber = escape(request.employeeNumber());
        String memo = escape(request.memo());
        String ipAddress = escape(request.ipAddress());

        // 한 페이지가 10개면 11개를 가져온다. 11번째가 있으면 "다음 페이지가 있다"는 뜻
        int limit = request.size() == Integer.MAX_VALUE ? Integer.MAX_VALUE : request.size() + 1;
        Pageable pageable = PageRequest.of(0, limit);

        boolean asc = request.sortDirection().isAscending();

        // 정렬 기준(IP/시간) × 방향(오름/내림)에 맞는 쿼리 고르기
        if ("ipAddress".equals(request.sortField())) {
            String lastIp = last == null ? null : last.getIpAddress();
            return asc
                    ? searchByIpAsc(employeeNumber, memo, ipAddress, request.type(),
                    request.from(), request.to(), lastId, lastIp, pageable)
                    : searchByIpDesc(employeeNumber, memo, ipAddress, request.type(),
                    request.from(), request.to(), lastId, lastIp, pageable);
        }

        Instant lastCreatedAt = last == null ? null : last.getCreatedAt();
        return asc
                ? searchByTimeAsc(employeeNumber, memo, ipAddress, request.type(),
                request.from(), request.to(), lastId, lastCreatedAt, pageable)
                : searchByTimeDesc(employeeNumber, memo, ipAddress, request.type(),
                request.from(), request.to(), lastId, lastCreatedAt, pageable);
    }

    // 검색어가 비어 있으면 null, 아니면 \, %, _ 앞에 \ 를 붙여서 그냥 글자로 취급
    private String escape(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}