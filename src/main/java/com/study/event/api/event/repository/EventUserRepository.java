package com.study.event.api.event.repository;

import com.study.event.api.event.entity.EventUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventUserRepository extends JpaRepository<EventUser, String> {

    // query method로 Jpql 생성
    boolean existsByEmail(String email);

    // 조회할 때 null 에러를 방지하기 위해 Optional 사용하기
    Optional<EventUser> findByEmail(String email);
}