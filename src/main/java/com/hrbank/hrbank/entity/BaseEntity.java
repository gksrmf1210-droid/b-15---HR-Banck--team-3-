package com.hrbank.hrbank.entity;


import jakarta.persistence.Column;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    // @MappedSuperclass : Java 상속을 JPA의 테이블 매핑에도 적용해. 자식 엔티티의 테이블에 부모의 공통 필드가 포함되도록 해.
    // Auditing : 엔티티가 생성되거나 수정될 때 시간을 자동으로 기록해. 단, 활성화 설정이 필요해.


    // 데이터가 처음 저장될 때 생성 시간을 자동 기록한다.
    @CreatedDate

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // 데이터가 수정될 때 마지막 수정 시간을 자동 기록한다.
    @LastModifiedDate

    // 수정 시간도 DB에 null로 저장되지 않도록 설정한다.
    @Column(nullable = false)
    private LocalDateTime updatedAt;


}
