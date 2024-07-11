package com.study.event.api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

// Spring 프레임워크에서 QueryDsl 을 사용하기 위한 설정클래스

// 프로젝트에 QueryDSL 을 사용할 때 이 클래스 무조건 생성해야 한다.
@Configuration // 1. 어노베이션을 통해 이 클래스가 설정정보를 포함하고 있음을 알려주기
public class QueryDslConfig {

    // 영속성을 제어하는 컨텍스트
    @PersistenceContext // 2. 어노베이션을 통해 EntityManager 을 주입받기
    // 3. EntityManager 는 JPA에서 엔터티의 영속성을 관리하며, DB와의 통신을 처리
    private EntityManager em;

    // 4. Spring Bean 에 등록시켜 외부 라이브러리를 스프링 컨테이너에 관리시키는 설정
    @Bean
    // xml에서는 @Bean 대신 이런식으로 작성
    // "<bean id='jpaqueryFactory' class='com.querydsl.jpa.impl.JPAQueryFactory>"
    // 5. 생성자를 통해 주입시키기
    public JPAQueryFactory jpaQueryFactory () {
        // 6. JPAQueryFactory 객체를 생성할 때, em을 인자로 전달하여 생성한다.
        return new JPAQueryFactory(em);
    }
}
