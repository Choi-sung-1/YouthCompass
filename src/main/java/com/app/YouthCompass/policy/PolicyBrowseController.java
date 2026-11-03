package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.recommendation.PolicyCategories;
import com.app.YouthCompass.policy.dto.PolicyListRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

// 정책 목록·상세 (로그인 없이 볼 수 있음, 추천은 PolicyController 담당)
@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyBrowseController {

    private final PolicyService policyService;

    // GET /policies?category=&region=&keyword=&openOnly=&page=
    @GetMapping
    public String list(@ModelAttribute("condition") PolicyListRequestDTO condition, Model model) {

        model.addAttribute("result", policyService.searchPolicies(condition));

        // 필터 선택지
        model.addAttribute("categoryOptions", PolicyCategories.ALL);
        model.addAttribute("regionOptions", Region.values());

        return "policy/list";
    }
}