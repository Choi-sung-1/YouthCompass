package com.app.YouthCompass.openAI;

import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import com.app.YouthCompass.policy.recommendation.PolicyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class openAiController {

    private final openAiService service;
    private final PolicyRecommendationService policyRecommendationService;

    @GetMapping(value = "/api/test/ai",
            produces = "application/json;charset=UTF-8")
    public List<PolicyEvaluationResult> test() {

        openAiService.ExtractionResult extracted = service.extract("""
                저는 25세이고 안양에 살아요.
                대학 재학중입니다.
                결혼은 안 했어요.
                정보보호학과를 나왔습니다.
                월세 지원 정책을 찾고 있어요
                """);

        return
                policyRecommendationService.recommend(extracted.profile(), extracted.intent());
    }

}
