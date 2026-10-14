package com.hrbank.hrbank;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class HrbankApplicationTests {


	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@Test
	void contextLoads() {
	}

	@Test
	void jpaQueryFactoryBeanIsRegistered() {
		assertNotNull(jpaQueryFactory);
	}

}
