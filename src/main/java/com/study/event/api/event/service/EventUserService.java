package com.study.event.api.event.service;

import com.study.event.api.auth.TokenProvider;
import com.study.event.api.event.dto.request.LoginRequestDto;
import com.study.event.api.event.dto.request.EventUserSaveDto;
import com.study.event.api.event.dto.response.LoginResponseDto;
import com.study.event.api.event.entity.EmailVerification;
import com.study.event.api.event.entity.EventUser;
import com.study.event.api.event.repository.EmailVerificationRepository;
import com.study.event.api.event.repository.EventUserRepository;
import com.study.event.api.exception.LoginFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventUserService {

    private final EventUserRepository eventUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Value("${study.mail.host}") // SpringFramework 의 @Value 로 가져오기
    private String mailHost;

    // 이메일 전송 객체
    private final JavaMailSender mailSender;

    // 패스워드 암호화 객체
    private final PasswordEncoder encoder;

    // 토큰 생성 객체
    private final TokenProvider tokenProvider;

    // 이메일 중복확인 처리
    public boolean checkEmailDuplicate(String email) {

        boolean exists = eventUserRepository.existsByEmail(email);
        log.info("Checking email {} is duplicate : {}", email, exists);

        // 중복인데 회원가입이 마무리되지 않은 회원은 중복이 아니라고 판단한다.
        if (exists && notFinish(email)) {

            return false;
        }

        // 일련의 후속 처리 (데이터베이스 처리, 이메일 보내는 것...)
        if (!exists) processSignUp(email);

        return exists;
    }

    private boolean notFinish(String email) {

        // 해당 회원을 찾기
        EventUser eventUser = eventUserRepository.findByEmail(email).orElseThrow();

        if (!eventUser.isEmailVerified() || eventUser.getPassword() == null) { // 이메일인증이 안끝났거나 비밀번호가 없는경우

            // 해당 회원의 인증코드
            EmailVerification ev = emailVerificationRepository.findByEventUser(eventUser).orElse(null);

            if (ev != null) { // 인증코드가 없는게 아니면 인증코드 삭제하기
                emailVerificationRepository.delete(ev);
            }

            // 인증코드 재발송하기
            generateAndSendCode(email, eventUser);

            return true;
        }
        return false; // 중복이 아니다.
    }

    public void processSignUp(String email) {

        // 1. 임시 회원가입
        EventUser newEventUser = EventUser
                .builder()
                .email(email)
                .build();

        EventUser savedUser = eventUserRepository.save(newEventUser);

        generateAndSendCode(email, savedUser);

    }

    private void generateAndSendCode(String email, EventUser eventUser) {
        // 2. 이메일 인증 코드 발송
        String code = sendVerificationEmail(email);

        // 3. 인증 코드 정보를 데이터베이스에 저장
        EmailVerification verification = EmailVerification.builder()
                .verificationCode(code) // 인증 코드
                .expiryDate(LocalDateTime.now().plusMinutes(5)) // 만료 시간 (5분 뒤)
                .eventUser(eventUser) // FK
                .build();

        emailVerificationRepository.save(verification);
    }

    // 이메일 인증 코드 보내기
    public String sendVerificationEmail(String email) {

        // 검증 코드 생성하기
        String code = generateVerificationCode();

        // 이메일을 전송할 객체 생성
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 누구에게 이메일을 보낼 것인지
            messageHelper.setTo(email);
            // 이메일 제목 설정
            messageHelper.setSubject("[인증메일] 중앙정보스터디 가입 인증 메일입니다.");
            // 이메일 내용 설정
            messageHelper.setText(
                    "인증 코드: <b style=\"font-weight: 700; letter-spacing: 5px; font-size: 30px;\">" + code + "</b>"
                    , true
            );

            // 전송자의 이메일 주소
            messageHelper.setFrom(mailHost);

            // 이메일 보내기
            mailSender.send(mimeMessage);

            log.info("{} 님에게 이메일 전송!", email);

            return code;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 검증 코드 생성 로직 1000~9999 사이의 4자리 숫자
    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 9000 + 1000));
    }

    // 인증코드 체크
    public boolean isMatchCode(String email, String code) {

        // 이메일을 통해 회원정보를 탐색
        EventUser eventUser = eventUserRepository.findByEmail(email)
                .orElse(null);

        if (eventUser != null) {
            // 인증코드가 있는지 탐색
            EmailVerification ev = emailVerificationRepository.findByEventUser(eventUser).orElse(null);

            // 인증코드가 있고 만료시간이 지나지 않았고 코드번호가 일치할 경우
            if (
                    ev != null
                            && ev.getExpiryDate().isAfter(LocalDateTime.now())
                            && code.equals(ev.getVerificationCode())
            ) {
                // 이메일 인증여부 true로 수정
                eventUser.setEmailVerified(true);
                eventUserRepository.save(eventUser); // UPDATE

                // 인증코드 데이터베이스에서 삭제
                emailVerificationRepository.delete(ev);
                return true;
            } else {  // 인증코드가 틀렸거나 만료된 경우
                // 인증코드 재발송
                // 원래 인증 코드 삭제
                emailVerificationRepository.delete(ev);

                // 새인증코드 발급 이메일 재전송
                // 데이터베이스에 새 인증코드 저장
                generateAndSendCode(email, eventUser);
                return false;
            }

        }
        return false;
    }

    // 회원가입 마무리
    public void confirmSignUp(EventUserSaveDto dto) {

        // 기존 회원 정보 조회
        EventUser foundUser = eventUserRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(
                        () -> new RuntimeException("회원 정보가 존재하지 않습니다.")
                );

        // 데이터 반영 (패스워드, 가입시간)
        String password = dto.getPassword();
        String encodedPassword = encoder.encode(password); // 암호화
        foundUser.confirm(encodedPassword);

        eventUserRepository.save(foundUser);
    }

    // 회원 인증 처리 (login)
    public LoginResponseDto authenticate (final LoginRequestDto dto) { // 매개변수에 final 붙이면 해당 변수는 다른 변수로 변경 불가
        // 이메일을 통해 회원정보 조회
        EventUser eventUser = eventUserRepository.findByEmail(dto.getEmail())
                .orElseThrow(()-> new LoginFailException("가입된 회원이 아닙니다."));

        // 이메일 인증을 안했거나 패스워드를 설정하지 않은 회원
        if (!eventUser.isEmailVerified() || eventUser.getPassword() == null) {
            throw new LoginFailException("회원가입이 중단된 회원입니다. 다시 가입해주세요.");
        }

        // 패스워드 검증하기
        String inputPassword = dto.getPassword(); // 방금 입력한 비밀번호
        String encodedPassword = eventUser.getPassword(); // DB 에 있는 암호화 된 비밀번호

        if (!encoder.matches(inputPassword, encodedPassword)) {
            throw new LoginFailException("비밀번호가 틀렸습니다.");
        }

        // 로그인 성공한 후,
        // 인증정보를 어떻게 관리할 것인가? 세션 or 쿠키 or 토큰
        // 인증정보 (이메일, 닉네임, 프사, 토큰정보) 를 클라이언트에게 전송

        // 기존에는 session 을 사용해 관리했었지만 리액트를 사용한 방법에서는 사용하기 불편하다.
        // 서버의 주소(8000번)와 클라이언트(리액트 3000번)의 주소가 다르다.
        // 웹사이트의 규모가 커졌을 경우 서버를 여러개 둬야 하는데,
        // 세션은 서버당 1개만 가질 수 있기 때문에 서버가 늘어날수록 세션개수도 늘어나야 하기 때문에 서버간 공유가 힘들다.

        // 쿠키는 브라우저에서만 사용가능해서 이것도 불편하다. (모바일에서 사용불가)

        // --> 토큰 사용 JWT (JSON Web Token)

        // 토큰 생성하기
        String token = tokenProvider.createToken(eventUser);

        return LoginResponseDto.builder()
                .email(eventUser.getEmail())
                .role(eventUser.getRole().toString())
                .token(token)
                .build();
    }

    // 등업 중간처리
    public LoginResponseDto promoteToPremium(String userId) {

        // 회원 탐색하기
        EventUser eventUser = eventUserRepository.findById(userId).orElseThrow(); // 못찾으면 에러나기 때문에 에러받아야 함.

        // 해당 회원 등급 변경하기
        eventUser.promoteToPremium(); // setter 도 권한부여가능하지만 메소드 이름으로 명확하게 뭐하는건지 지정
        EventUser promotedUser = eventUserRepository.save(eventUser);

        // 등급 변경한 내용을 업데이트한 토큰 재발급
        String token = tokenProvider.createToken(promotedUser);

        return LoginResponseDto.builder()
                                            .token(token)
                                            .role(promotedUser.getRole().toString())
                                            .email(promotedUser.getEmail())
                                            .build();

    }
}