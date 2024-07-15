package com.study.event.api.auth;

import com.study.event.api.event.entity.EventUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TokenProvider {

    /**
     * JWT 를 생성하는 메서드
     * @param eventUser - 토큰에 포함될 로그인한 유저의 정보
     * @return - 생성된 JWT의 암호화된 문자열
     */

    // 서명에 사용할 512비트의 랜덤 문자열 비밀키
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String createToken (EventUser eventUser) {

        /*
            토큰의 형태
            {
                "iss": "뽀로로월드" // 토큰의 발급자
                "exp": "2024-07-18", // 토큰 만료시간
                "iat": "2024-07-15", // 토큰 발행시간
                ...
                "email": "로그인한 사람 이메일" // 토큰 어디로 발행했는지 이메일
                "role": "ADMIN"
                ...
                ===
                서명
            }
         */

        // 토큰에 들어갈 커스텀 데이터 (추가 클레임)
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", eventUser.getEmail());
        claims.put("role", eventUser.getRole().toString());

//        System.out.println("SECTET_KEY = " + SECRET_KEY);

        return Jwts.builder()
                // 1. token에 들어갈 서명
                .signWith(
                        Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
                        , SignatureAlgorithm.HS512
                )
                // 3. payload 에 들어갈 클레임 생성 (token 에 넣어 줄 추가 내용, 추가클레임은 항상 가장 먼저 설정해야 한다.)
                .setClaims(claims)
                // 2. payload 에 들어갈 클레임 생성 (token 의 필수 내용)
                .setIssuer("폼폼푸린") // 발급자 정보
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))) // 토큰 만료시간 (1일 후)
                .setSubject(eventUser.getId()) // 토큰을 식별할 수 있는 유일한 값
                .compact();
    }
}