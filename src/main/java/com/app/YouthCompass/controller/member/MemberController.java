package com.app.YouthCompass.controller.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;
import com.app.YouthCompass.domain.vo.Region;
import com.app.YouthCompass.service.member.MemberService;
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
        return "/member/join";
    }
    @PostMapping("/join")
    public String join(@ModelAttribute("member") MemberJoinRequestDTO member){
        memberService.join(member);
        return "redirect:/member/login";
    }
    @GetMapping("/login")
    public String login(){
        return "/member/login";
    }
}
