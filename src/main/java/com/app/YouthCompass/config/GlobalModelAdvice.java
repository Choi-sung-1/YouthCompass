package com.app.YouthCompass.config;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 모든 화면(상단 메뉴)에서 로그인 여부·관리자 여부를 쓸 수 있게 모델에 공통으로 넣어 줌
@ControllerAdvice
public class GlobalModelAdvice {

    // authentication: 로그인 안 했으면 null
    @ModelAttribute
    public void addLoginInfo(Authentication authentication, Model model) {

        model.addAttribute("loginId", authentication != null ? authentication.getName() : null);

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("isAdmin", isAdmin);
    }
}