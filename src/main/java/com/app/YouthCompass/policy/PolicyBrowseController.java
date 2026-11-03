package com.app.YouthCompass.policy;

import com.app.YouthCompass.member.MemberService;
import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.recommendation.PolicyCategories;
import com.app.YouthCompass.policy.dto.PolicyDetailResponseDTO;
import com.app.YouthCompass.policy.dto.PolicyListRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

// 정책 목록·상세 (로그인 없이 볼 수 있음, 추천은 PolicyController 담당)
@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyBrowseController {

    private final PolicyService policyService;
    private final PolicyDetailService policyDetailService;
    private final MemberService memberService;   // B 담당 — findMember 호출만

    // GET /policies?category=&region=&keyword=&openOnly=&page=
    @GetMapping
    public String list(@ModelAttribute("condition") PolicyListRequestDTO condition, Model model) {

        model.addAttribute("result", policyService.searchPolicies(condition));

        // 필터 선택지
        model.addAttribute("categoryOptions", PolicyCategories.ALL);
        model.addAttribute("regionOptions", Region.values());

        return "policy/list";
    }
    // GET /policies/{id} — 정책 상세 (로그인 회원이면 조건별 판정까지)
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {

        MemberVO member = principal == null
                ? null
                : memberService.findMember(principal.getName()).orElse(null);

        PolicyDetailResponseDTO detail = policyDetailService.getDetail(id, member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "정책을 찾을 수 없습니다."));

        model.addAttribute("detail", detail);
        return "policy/detail";
    }
}