package com.study.event.api.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginFailException extends RuntimeException {

    // 로그인 실패시 발생하는 오류 처리하기
    public LoginFailException (String message) {
        super(message);
    }

}
