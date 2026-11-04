package com.app.YouthCompass.bookmark;

import com.app.YouthCompass.member.MemberService;
import com.app.YouthCompass.member.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.Principal;

// 찜 토글·찜 목록 (둘 다 로그인 필요 — SecurityConfig의 anyRequest().authenticated())
@Slf4j
@Controller
@RequiredArgsConstructor
public class BookmarkController {

    // 돌아갈 곳을 정하지 못했을 때 기본 화면
    private static final String DEFAULT_RETURN_PATH = "/mypage/bookmarks";

    private final BookmarkService bookmarkService;
    private final MemberService memberService;

    // POST /bookmarks/{policyId} — 찜 토글 후 원래 보던 화면으로
    @PostMapping("/bookmarks/{policyId}")
    public String toggle(
            @PathVariable Long policyId,
            @RequestParam(required = false) String redirect,
            @RequestHeader(value = "Referer", required = false) String referer,
            Principal principal
    ) {
        // TODO: A의 PolicyService에 정책 단건 조회가 생기면 없는 정책 번호는 여기서 막기
        //       (지금은 찜 목록 쿼리의 TBL_POLICY JOIN에서 걸러짐)
        bookmarkService.toggleBookmark(loginMemberId(principal), policyId);

        return "redirect:" + returnPath(redirect, referer);
    }

    // GET /mypage/bookmarks — 찜 목록 (최근에 찜한 순, D-day 포함)
    @GetMapping("/mypage/bookmarks")
    public String list(Principal principal, Model model) {

        model.addAttribute("bookmarks", bookmarkService.findBookmarkedPolicies(loginMemberId(principal)));

        return "mypage/bookmarks";
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

    // 돌아갈 곳: redirect 파라미터 → Referer → 찜 목록
    // 외부 사이트로 보내지 않도록 우리 사이트 안의 경로만 사용
    private String returnPath(String redirect, String referer) {

        if (isInternalPath(redirect)) {
            return redirect;
        }

        if (referer != null) {
            try {
                // Referer는 전체 주소(http://localhost:8080/policies?page=2) → 경로와 쿼리만 사용
                URI uri = URI.create(referer);
                String path = uri.getRawPath();
                if (isInternalPath(path)) {
                    return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
                }
            } catch (IllegalArgumentException e) {
                log.debug("잘못된 Referer 무시: {}", referer);
            }
        }

        return DEFAULT_RETURN_PATH;
    }

    // "/"로 시작하는 내부 경로만 허용 ("//evil.com", "/\evil.com"은 브라우저가 외부 주소로 해석하므로 차단)
    private boolean isInternalPath(String path) {
        return path != null
                && path.startsWith("/")
                && !path.startsWith("//")
                && !path.startsWith("/\\");
    }
}