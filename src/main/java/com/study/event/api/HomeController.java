package com.study.event.api;

import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {

    @GetMapping("/")
    public String home() {
        return "도커로 구동확인!!";
    }
}
