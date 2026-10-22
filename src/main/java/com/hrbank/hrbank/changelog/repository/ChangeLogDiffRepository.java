package com.hrbank.hrbank.changelog.repository;

import com.hrbank.hrbank.changelog.dto.response.ChangeLogDiffResponse;
import com.hrbank.hrbank.changelog.entity.ChangeLogDiff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChangeLogDiffRepository extends JpaRepository<ChangeLogDiff, Long> {

    @Query("""
            SELECT new com.hrbank.hrbank.changelog.dto.response.ChangeLogDiffResponse(
                d.propertyName, d.beforeValue, d.afterValue)
            FROM ChangeLogDiff d
            WHERE d.changeLog.id = :changeLogId
            ORDER BY d.id
            """)
    List<ChangeLogDiffResponse> findDiffs(@Param("changeLogId") Long changeLogId);
}