package com.hrbank.hrbank.changelog.repository;

import com.hrbank.hrbank.changelog.entity.ChangeLogDiff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeLogDiffRepository extends JpaRepository<ChangeLogDiff, Long> {
    List<ChangeLogDiff> findByChangeLogId(Long changeLogId);
}
