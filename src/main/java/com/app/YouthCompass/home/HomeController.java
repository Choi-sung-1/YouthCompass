package com.app.YouthCompass.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 로그인 여부(loginId)·관리자 여부(isAdmin)는 GlobalModelAdvice가 모든 화면에 넣어 줌
    @GetMapping("/")
    public String home() {
        return "HomePage";
    }
}