package com.study.event.api.event.service;

import com.study.event.api.event.entity.EventUser;
import com.study.event.api.event.repository.EventUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventUserService {

    private final EventUserRepository eventUserRepository;

    // 이메일 중복확인 처리
    public boolean checkEmailDuplicate(String email) {

        EventUser user = EventUser.builder()
                .email("abc@def.com" + (int) (Math.random() * 10))
                .build();
        eventUserRepository.save(user);

        boolean exists = eventUserRepository.existsByEmail(email);
        log.info("Checking email {} is duplicate : {}", email, exists);
        return exists;
    }

}
