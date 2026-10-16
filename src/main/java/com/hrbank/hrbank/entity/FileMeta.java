package com.hrbank.hrbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Getter
@Entity
@Table(name = "file_meta")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMeta extends BaseEntity {

    @Id
    private UUID id;

    @Column(length = 255)
    private String fileName;

    @Column(length = 255)
    String contentType;

    @Column(length = 255)
    private long  size;

    public FileMeta(
            UUID id,
            String fileName,
            String contentType,
            long size) {

        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size  = size;


    }
}
