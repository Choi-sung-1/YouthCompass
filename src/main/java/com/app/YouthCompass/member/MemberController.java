package com.app.YouthCompass.member;

import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.member.dto.MemberJoinRequestDTO;
import com.app.YouthCompass.profile.dto.SigunguGroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member/*")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/join")
    public String join(Model model){
        model.addAttribute("member", new MemberJoinRequestDTO());
        model.addAttribute("regions", Region.values());
        // 시·도별 시·군 목록 (화면에서 지역 선택에 따라 걸러서 보여줌)
        model.addAttribute("sigunguGroups", SigunguGroupDTO.all());
        return "member/join";
    }
    @PostMapping("/join")
    public String join(@ModelAttribute("member") MemberJoinRequestDTO member){
        memberService.join(member);
        return "redirect:/member/login";
    }
    @GetMapping("/login")
    public String login(){
        return "member/login";
    }
}
