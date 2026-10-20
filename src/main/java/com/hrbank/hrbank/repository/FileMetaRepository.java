package com.hrbank.hrbank.repository;


import com.hrbank.hrbank.entity.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileMetaRepository extends JpaRepository<FileMeta, Long> {


}
