package com.app.YouthCompass.history;

import com.app.YouthCompass.bookmark.BookmarkService;
import com.app.YouthCompass.history.dto.RecommendHistoryDetailDTO;
import com.app.YouthCompass.member.MemberService;
import com.app.YouthCompass.member.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// 추천 기록 목록·상세 (로그인 필요 — SecurityConfig의 anyRequest().authenticated())
// 마이페이지 본 화면(/mypage, /mypage/profile)은 profile/MypageController 담당
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/history")
public class RecommendHistoryController {

    private final RecommendHistoryService historyService;
    private final MemberService memberService;
    private final BookmarkService bookmarkService;

    // GET /mypage/history — 내 추천 기록 목록 (최근 순)
    @GetMapping
    public String list(Principal principal, Model model) {

        model.addAttribute("histories", historyService.findHistories(loginMemberId(principal)));

        return "mypage/history";
    }

    // GET /mypage/history/{historyId} — 추천 기록 상세 (저장된 TOP 10, AI 재호출 없음)
    @GetMapping("/{historyId}")
    public String detail(@PathVariable Long historyId, Principal principal, Model model) {

        Long memberId = loginMemberId(principal);

        // 남의 기록 번호이거나 없는 번호면 404
        RecommendHistoryDetailDTO detail = historyService.findHistoryDetail(historyId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 기록을 찾을 수 없습니다."));

        model.addAttribute("detail", detail);
        // 카드의 찜 버튼 상태용 (찜 목록 화면과 같은 BookmarkService 호출)
        model.addAttribute("bookmarkedIds", bookmarkService.findBookmarkedPolicyIds(memberId));
        return "mypage/history-detail";
    }

    // POST /mypage/history/{historyId}/delete — 추천 기록 삭제 후 목록으로
    @PostMapping("/{historyId}/delete")
    public String delete(@PathVariable Long historyId, Principal principal, RedirectAttributes redirectAttributes) {

        // 남의 기록 번호이거나 없는 번호면 404 (상세 화면과 같은 기준)
        if (!historyService.delete(historyId, loginMemberId(principal))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 기록을 찾을 수 없습니다.");
        }

        redirectAttributes.addFlashAttribute("deleted", true);
        return "redirect:/mypage/history";
    }

    // 로그인 회원 PK (로그인 필수 URL이므로 principal이 없으면 비정상 접근)
    private Long loginMemberId(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return memberService.findMember(principal.getName())
                .map(MemberVO::getMemberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다."));
    }
}