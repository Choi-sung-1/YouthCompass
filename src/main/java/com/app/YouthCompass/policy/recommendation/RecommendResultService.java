package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.openAI.openAiService;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.dto.RecommendResponseDTO;
import com.app.YouthCompass.policy.dto.RecommendedPolicyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// 추천 화면에 필요한 결과를 만드는 서비스
// AI 조건 추출(openAiService) → 정책 추천(PolicyRecommendationService) → 상위 10개 → DTO
@Service
@RequiredArgsConstructor
public class RecommendResultService {

    // 화면에 보여줄 추천 정책 개수
    private static final int TOP_N = 10;

    private final openAiService aiService;
    private final PolicyRecommendationService policyRecommendationService;
    private final RecommendReasonBuilder reasonBuilder;

    // 사용자 문장으로 추천 (AI 호출 O)
    public RecommendResponseDTO recommendFromSentence(String sentence) {

        openAiService.ExtractionResult extracted = aiService.extract(sentence);

        return recommendFromProfile(extracted.profile(), extracted.intent());
    }

    // 이미 정해진 조건으로 추천 (AI 호출 X)
    // 12번에서 사용자가 조건을 고친 뒤 다시 추천할 때도 사용
    public RecommendResponseDTO recommendFromProfile(
            UserPolicyProfileVO profile,
            PolicySearchIntent intent
    ) {

        List<RecommendedPolicyDTO> policies =
                policyRecommendationService.recommend(profile, intent)
                        .stream()
                        .limit(TOP_N)
                        .map(result -> toDto(result, profile, intent))
                        .toList();

        return new RecommendResponseDTO(profile, intent, policies);
    }

    // 판정 결과 → 카드 DTO + 추천 이유 / 확인 필요 조건
    private RecommendedPolicyDTO toDto(
            PolicyEvaluationResult result,
            UserPolicyProfileVO profile,
            PolicySearchIntent intent
    ) {

        RecommendedPolicyDTO dto = RecommendedPolicyDTO.from(result);
        dto.setReasons(reasonBuilder.reasons(result, profile, intent));
        dto.setUnknownReasons(reasonBuilder.unknownReasons(result, profile));

        return dto;
    }
}
