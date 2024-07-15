package com.study.event.api.event.controller;

import com.study.event.api.event.dto.request.EventUserSaveDto;
import com.study.event.api.event.dto.request.LoginRequestDto;
import com.study.event.api.event.dto.response.LoginResponseDto;
import com.study.event.api.event.service.EventUserService;
import com.study.event.api.exception.LoginFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 인증 코드 검증 API
    @GetMapping("/code")
    public ResponseEntity<?> verifyCode(String email, String code) {

        log.info("{}'s verify code is [ {} ]", email, code);
        boolean isMatch = eventUserService.isMatchCode(email, code);

        return ResponseEntity.ok().body(isMatch);

        // postman
        // get, http://localhost:8787/auth/code?email=aaa@gmail.com&code=이메일에서 받은 코드
        // 이메일 없을 경우 false, 있으면 true
    }

    // 회원가입 마무리 처리
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody EventUserSaveDto dto) {

        log.info("saved User Info - {}", dto);

        try {
            eventUserService.confirmSignUp(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().body("saved success");

        // post, Body - raw - JSON, http://localhost:8787/auth/join
        /*
            {
                "email": "aaa@gmail.com",
                "password": "abc1234!"
            }
         */
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> SignIn (@RequestBody LoginRequestDto dto) {

        // authenticate 메소드에서 throw 오류 하기 때문에 try catch 해줘야 한다. 여기서도 throw 하면 안됨.
        try {
            // 로그인 성공시
            LoginResponseDto responseDto = eventUserService.authenticate(dto);
            return ResponseEntity.ok().body(responseDto);

        } catch (LoginFailException e) {
            // 서비스에서 예외 발생 (로그인 실패시)
            String errorMessage = e.getMessage();
            return ResponseEntity.status(422) // 번호는 내가 커스텀해도 됨 (로그인실패는 422하겠다)
                    .body(errorMessage);
        }

        // React 에서 회원가입 후 postman 에서 로그인 시도하기
        // post, Body - raw - JSON, http://localhost:8787/auth/sign-in
        /*
            {
                "email": "aaa@gmail.com",
                "password": "abc1234!"
            }
         */
    }
}
