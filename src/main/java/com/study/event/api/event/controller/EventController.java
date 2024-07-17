package com.study.event.api.event.controller;

import com.study.event.api.auth.TokenProvider;
import com.study.event.api.event.dto.request.EventSaveDto;
import com.study.event.api.event.dto.response.EventOneDto;
import com.study.event.api.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.study.event.api.auth.TokenProvider.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    // 전체 조회 요청
    @SneakyThrows
    @GetMapping("/page/{pageNo}")
    public ResponseEntity<?> getList(
            // 토큰 파싱 결과로 로그인에 성공한 회원의 PK
            @AuthenticationPrincipal TokenUserInfo tokenInfo,
            @RequestParam(required = false) String sort,
            @PathVariable int pageNo) throws InterruptedException {

        log.info("tokenInfo: {}", tokenInfo);

        if (sort == null) {
            return ResponseEntity.badRequest().body("sort 파라미터가 없습니다.");
        }

        Map<String, Object> events = eventService.getEvents(pageNo, sort, tokenInfo.getUserId());

        // 의도적으로 2초간의 로딩을 설정
//        Thread.sleep(2000);

        return ResponseEntity.ok().body(events);
    }

    // PostMan
    // Get, http://localhost:8282/events/page/2?sort=date (2가 페이지번호, sort=date 필수)

    // 등록 요청
    @PostMapping
    public ResponseEntity<?> register (@AuthenticationPrincipal TokenUserInfo userInfo, // JwtAuthFilter 에서 시큐리티에 등록한 데이터
                                      @RequestBody EventSaveDto dto) {
        eventService.saveEvent(dto, userInfo.getUserId());
        return ResponseEntity.ok().body("event saved");
    }

    // 단일 조회 요청
    @PreAuthorize("hasAuthority('PREMIUM') or hasAuthority('ADMIN')") // 사전에 인가받은 여부 (프리미엄회원만 상세조회 가능)
    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEvent (@PathVariable Long eventId) {

        if (eventId == null || eventId < 1) {

            String errorMessage = "eventId 가 정확하지 않습니다.";
            log.warn(errorMessage);

            return ResponseEntity.badRequest().body(errorMessage);
        }

        EventOneDto eventOne = eventService.getEventDetail(eventId);

        return ResponseEntity.ok().body(eventOne);
    }

    // PostMan
    // Get, http://localhost:8282/events/2 - 건강건강이벤트

    // 삭제요청
    
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> delete (@PathVariable Long eventId) {

        eventService.deleteEvent(eventId);

        return ResponseEntity.ok().body("event deleted");
    }

    // PostMan
    // DELETE, http://localhost:8282/events/7 (url id 7번 게시글 삭제)

    // 수정요청
    @PatchMapping("/{eventId}")
    public ResponseEntity<?> modity(@RequestBody EventSaveDto dto, @PathVariable Long eventId) {
        eventService.modifyEvent(dto, eventId);

        return ResponseEntity.ok().body("event modified!!");
    }

    // PostMan
    // PATCH, http://localhost:8282/events/6 (url id 7번 게시글 수정)
    /*
    {
        "title": "농부 이벤트",
            "desc": "농부 이벤트입니다. 건강하겠지?",
            "imageUrl": "https://www.nhis.or.kr/static/alim/paper/oldpaper/202109/assets/images/sub/event01_mo.jpg",
            "beginDate": "2024-12-31"
    }
    */
}