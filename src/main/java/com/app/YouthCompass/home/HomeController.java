package com.app.YouthCompass.home;

import com.app.YouthCompass.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PolicyService policyService;

    // 로그인 여부(loginId)·관리자 여부(isAdmin)는 GlobalModelAdvice가 모든 화면에 넣어 줌
    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("home", policyService.getHomeWidgets());

        return "HomePage";
    }
}