package com.app.YouthCompass.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.core.Authentication;

@Controller
public class HomeController {

    // authentication: 로그인한 사람의 정보. 로그인하지 않았으면 Spring이 null을 넣어줌
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {

        // 화면에서 로그인 여부에 따라 회원가입·로그인 / 로그아웃 버튼을 바꿔 보여줌
        model.addAttribute("loginId", authentication != null ? authentication.getName() : null);

        // 관리자면 정책 동기화 버튼을 보여줌
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("isAdmin", isAdmin);

        return "HomePage";
    }
}
