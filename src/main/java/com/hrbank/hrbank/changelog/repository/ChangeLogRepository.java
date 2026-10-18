package com.hrbank.hrbank.changelog.repository;

import com.hrbank.hrbank.changelog.dto.request.ChangeLogSearchRequest;
import com.hrbank.hrbank.changelog.dto.response.ChangeLogResponse;
import com.hrbank.hrbank.changelog.entity.ChangeLog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Nulls;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long>, JpaSpecificationExecutor<ChangeLog> {

    // 이력 개수 세기
    long countByCreatedAtBetween(Instant from, Instant to);

    // 이력 목록 검색
    // 사용자가 입력한 조건(사번, 메모, IP, 시간, 유형)에 맞는 이력만 골라서,원하는 순서(IP,시간)로 한 페이지만큼 가져온다.
    default List<ChangeLogResponse> search(ChangeLogSearchRequest request) {

        // 두 번째 페이지부터는 "지난 페이지의 마지막 이력"이 어디였는지 알아야
        // 그 다음 이력부터 보여줄 수 있다.
        // request.after()는 그 마지막 이력의 번호(ID)이다.
        //   - 번호가 없으면 → 첫 페이지
        //   - 번호가 있으면 → DB에서 그 이력을 찾아서 IP, 시간 같은 정보를 꺼내 둔다.
        //   - 찾아봤는데 없으면 → 에러를 낸다(이미 지워졌거나 잘못된 번호).
        ChangeLogResponse last = request.after() == null ? null
                : findBy((Specification<ChangeLog>) (root, query, builder) ->
                        builder.equal(root.get("id"), request.after()),
                query -> query.as(ChangeLogResponse.class).one())
                .orElseThrow(() -> new NoSuchElementException("이전 페이지의 마지막 이력을 찾을 수 없습니다."));

        // 특정 조건만 가져오라고 DB에게 보낼 조건들을 하나씩 담는다.
        Specification<ChangeLog> conditions = (root, query, builder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 글자 포함 검색: 사번, 메모, IP 주소
            addContains(predicates, builder, root.get("employeeNumber"), request.employeeNumber());
            addContains(predicates, builder, root.get("memo"), request.memo());
            addContains(predicates, builder, root.get("ipAddress"), request.ipAddress());

            // 시간 범위 검색: from 이후, to 이전에 만들어진 이력만
            //     (시작 시간, 끝 시간은 각각 입력했을 때만 조건에 넣는다)
            if (request.from() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), request.from()));
            }
            if (request.to() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), request.to()));
            }

            // 유형 검색: 추가/수정/삭제 중 해당하는 친구만
            if (request.type() != null) {
                predicates.add(builder.equal(root.get("type"), request.type()));
            }

            // 정렬 방식
            //     ascending = true면 작은 것부터(오름차순), false면 큰 것부터(내림차순)
            //     sortByIp = true면 IP 주소 순서, false면 시간 순서
            boolean ascending = request.sortDirection().isAscending();
            boolean sortByIp = "ipAddress".equals(request.sortField());

            //     정렬 기준이 IP인지 시간인지에 따라 비교할 값이 달라짐
            if (last != null) {
                predicates.add(sortByIp
                        ? after(builder, root, root.<String>get("ipAddress"), last.ipAddress(), last.id(), ascending)
                        : after(builder, root, root.<Instant>get("createdAt"), last.createdAt(), last.id(), ascending));
            }

            // 실제로 정렬 적용하기
            //     1순위: IP 또는 시간 (값이 비어 있는 것은 방향과 상관없이 맨 뒤로)
            //     2순위: ID (1순위 값이 똑같은 이력끼리도 항상 같은 순서가 되도록)
            Expression<?> sortField = root.get(sortByIp ? "ipAddress" : "createdAt");
            query.orderBy(
                    ascending ? builder.asc(sortField, Nulls.LAST) : builder.desc(sortField, Nulls.LAST),
                    ascending ? builder.asc(root.get("id")) : builder.desc(root.get("id")));

            // 바구니에 담은 조건들을 전부 AND"로 묶어서 돌려준다.
            // → 조건이 여러 개면 모두 만족하는 이력만 나온다.
            return builder.and(predicates.toArray(Predicate[]::new));
        };

        // 몇 개를 가져올지 정하기
        // 한 페이지가 10개라면 11개를 가져온다.
        // 11번째가 있으면 "다음 페이지가 있다"는 걸 알 수 있기 때문
        int limit = request.size() == Integer.MAX_VALUE ? Integer.MAX_VALUE : request.size() + 1;

        // DB에서 가져오기
        // 위의 조건 상자로 검색해서, limit개만큼, 화면에 보여줄 모양(ChangeLogResponse)으로 받아온다.
        return findBy(conditions, query -> query.as(ChangeLogResponse.class).limit(limit).all());
    }

    // 글자 포함 조건 하나 만들기 (도우미)
    // field칸에 value 즉, 검색어가 들어 있는 것만 찾는 조건을 predicates에 넣는다.
    //  메모 칸에 "부서"가 들어 있으면 "부서이동"도 찾아지게끔
    // ============================================================
    private void addContains(List<Predicate> predicates, CriteriaBuilder builder,
                             Expression<String> field, String value) {
        // 검색어가 없거나 비어 있으면 조건을 만들지 않는다.
        if (value != null && !value.isEmpty()) {
            // 사용자가 진짜 % 글자를 찾고 싶을 수도 있으니, 앞에 \ 를 붙여서 글자로 취급
            String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            // 앞뒤에 %를 붙이면 "앞뒤에 뭐가 있든 상관없이 이 글자가 들어 있으면 OK"라는 뜻이 된다.
            predicates.add(builder.like(field, "%" + escaped + "%", '\\'));
        }
    }

    // 다음페이지를 요청할 때 1페이지에서 이미 보여준 이력을 빼고, 그다음 이력부터 가져오려고
    private <T extends Comparable<? super T>> Predicate after(
            CriteriaBuilder builder, Root<ChangeLog> root, Expression<T> field,
            T value, Long id, boolean ascending) {

        // "번호(ID)가 이전 페이지의 마지막보다 뒤인가?" (오름차순이면 더 큰 번호, 내림차순이면 더 작은 번호)
        Predicate nextId = ascending
                ? builder.greaterThan(root.get("id"), id)
                : builder.lessThan(root.get("id"), id);

        // 책갈피 이력의 정렬 값이 비어 있는(null) 경우
        if (value == null) {
            return builder.and(builder.isNull(field), nextId);
        }

        // 정렬 값이 이전 페이지의 마지막 값 보다 뒤인가? (오름차순이면 더 큰 값, 내림차순이면 더 작은 값)
        Predicate nextValue = ascending ? builder.greaterThan(field, value) : builder.lessThan(field, value);

        // 정렬 값이 이전 페이지의 마지막보다 뒤인 경.
        // 정렬 값은 이전 페이지의 마지막와 똑같지만, 번호표(ID)가 뒤인 경우
        // 정렬 값이 빈 경우 (비어 있는 것은 항상 맨 뒤라서 무조건 뒤에 있다).
        return builder.or(nextValue, builder.and(builder.equal(field, value), nextId), builder.isNull(field));
    }
}