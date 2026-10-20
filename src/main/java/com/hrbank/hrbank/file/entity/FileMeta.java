package com.hrbank.hrbank.file.entity;

import com.hrbank.hrbank.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


import java.util.UUID;


@Getter
@Entity
@Table(name = "file_meta")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMeta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String fileName;


    @Column(length = 255)
    private String contentType;

    @Column(nullable = false)
    private long  size;

    @Column(nullable = false, unique = true)
    private UUID storageKey;

    public FileMeta(
            UUID storageKey,
            String fileName,
            String contentType,
            long size)
    {


        this.storageKey = storageKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size  = size;


    }
}
