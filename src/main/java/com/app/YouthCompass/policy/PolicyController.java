package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.dto.RecommendRequestDTO;
import com.app.YouthCompass.policy.recommendation.RecommendResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/policy")
public class PolicyController {

    private final RecommendResultService recommendResultService;

    // 추천 페이지 (입력창만 있는 처음 화면)
    @GetMapping("/recommend")
    public String recommendPage(Model model) {

        model.addAttribute("request", new RecommendRequestDTO());

        return "policy/recommend";
    }

    // 문장 입력 → AI 조건 추출 → 추천 → 상위 10개를 같은 화면에 표시
    @PostMapping("/recommend")
    public String recommend(
            @ModelAttribute("request") RecommendRequestDTO request,
            Model model
    ) {

        try {
            model.addAttribute(
                    "result",
                    recommendResultService.recommendFromSentence(request.getSentence())
            );

        } catch (IllegalArgumentException e) {
            // 빈 문장, 4000자 초과 등 입력 문제 → 메시지를 그대로 보여줌
            model.addAttribute("errorMessage", e.getMessage());

        } catch (RuntimeException e) {
            // AI 호출 실패 등 → 자세한 내용은 로그로만 남김
            log.error("정책 추천 실패", e);
            model.addAttribute(
                    "errorMessage",
                    "추천 중 문제가 생겼어요. 잠시 후 다시 시도해 주세요."
            );
        }

        return "policy/recommend";
    }
}
