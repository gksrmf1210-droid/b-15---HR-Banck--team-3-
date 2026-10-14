package com.hrbank.hrbank.config;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//스프링 설정 클래스
@Configuration
public class QuerydslConfig {

    // JPA가 관리하는 EntityManager 주입 받음.
    @PersistenceContext
    private EntityManager entityManager;

    // 반환한 JPAQueryFactory 객체를 스프링 빈으로 등록함.
    // JPAQueryFactory : QueryDSL JPA 조회 쿼리를 작성할 때 사용하는 객체
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);

    }


}
