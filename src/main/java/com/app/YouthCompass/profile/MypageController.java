package com.app.YouthCompass.profile;

import com.app.YouthCompass.member.MemberService;
import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.profile.dto.MemberProfileFormDTO;
import com.app.YouthCompass.profile.dto.SigunguGroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

// 마이페이지: 내 정보 + 조건 프로필
// (찜 목록 /mypage/bookmarks, 추천 기록 /mypage/history 는 각자 다른 컨트롤러)
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MemberService memberService;
    private final MemberProfileService memberProfileService;

    @GetMapping
    public String mypage(Principal principal, Model model) {

        MemberVO member = loginMember(principal);

        MemberProfileFormDTO form = memberProfileService.findProfile(member.getMemberId())
                .map(MemberProfileFormDTO::from)
                .orElseGet(MemberProfileFormDTO::new);

        addPageData(model, member, form);
        return "mypage/index";
    }

    @PostMapping("/profile")
    public String saveProfile(
            @ModelAttribute("profileForm") MemberProfileFormDTO form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        MemberVO member = loginMember(principal);

        // 연소득 칸에 숫자가 아닌 값·음수 → 같은 화면에 안내
        if (bindingResult.hasErrors()
                || (form.getAnnualIncome() != null && form.getAnnualIncome() < 0)) {
            model.addAttribute("profileError", "연소득은 0 이상 숫자(만원)로 입력해 주세요.");
            addPageData(model, member, form);
            return "mypage/index";
        }

        memberProfileService.saveProfile(form.toVO(member.getMemberId()));
        redirectAttributes.addFlashAttribute("profileSaved", true);

        return "redirect:/mypage";
    }

    // /mypage 는 로그인 필수(SecurityConfig)라 principal은 항상 있음
    private MemberVO loginMember(Principal principal) {

        return memberService.findMember(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 회원 정보를 찾을 수 없습니다."));
    }

    private void addPageData(Model model, MemberVO member, MemberProfileFormDTO form) {

        model.addAttribute("member", member);
        model.addAttribute("regionName", Region.fromCode(member.getMemberRegion())
                .map(Region::getRegionName)
                .orElse(null));
        model.addAttribute("profileForm", form);

        model.addAttribute("sigunguGroups", SigunguGroupDTO.all());
        model.addAttribute("schoolOptions", userChoices(SchoolRequirement.values()));
        model.addAttribute("jobOptions", userChoices(JobRequirement.values()));
        model.addAttribute("marriageOptions", userChoices(MarriageRequirement.values()));
        model.addAttribute("majorOptions", userChoices(MajorRequirement.values()));
    }

    // 사용자가 고를 수 있는 값만 (알 수 없음·제한없음 제외 — 추천 조건 수정 폼과 같은 기준)
    private <E extends Enum<E>> List<E> userChoices(E[] values) {

        return Arrays.stream(values)
                .filter(v -> !v.name().equals("UNKNOWN") && !v.name().equals("NO_RESTRICTION"))
                .toList();
    }
}