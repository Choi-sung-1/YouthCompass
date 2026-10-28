package com.app.YouthCompass.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    // principal: 로그인한 사람의 정보. 로그인하지 않았으면 Spring이 null을 넣어줌
    @GetMapping("/")
    public String home(Principal principal, Model model) {

        // 화면에서 로그인 여부에 따라 회원가입·로그인 / 로그아웃 버튼을 바꿔 보여줌
        model.addAttribute("loginId", principal != null ? principal.getName() : null);

        return "HomePage";
    }
}
