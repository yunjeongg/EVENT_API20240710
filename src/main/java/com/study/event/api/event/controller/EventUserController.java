package com.study.event.api.event.controller;

import com.study.event.api.event.service.EventUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class EventUserController {

    private final EventUserService eventUserService;

    // 이메일 중복확인 API
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(String email) {
        boolean isDuplicate = eventUserService.checkEmailDuplicate(email);

        // 중복된 이메일이 아니면 인증코드메일 발송
//        if (!isDuplicate) {
//            eventUserService.sendVerificationEmail(email);
//        }

        return ResponseEntity.ok().body(isDuplicate);

        // postman
        // get, http://localhost:8787/auth/check-email?email=aaa@gmail.com
        // EventUserService 에서 등록 요청한 이메일이 DB 에 없을 경우 false, 있으면 true
        // aaa@gmail.com 에 application.yml에서 작성한 이메일로부터 인증코드 메일 가는걸 확인할 수 있다.
    }


}
