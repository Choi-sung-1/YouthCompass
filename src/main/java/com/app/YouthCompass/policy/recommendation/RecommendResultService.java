package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.openAI.openAiService;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.dto.RecommendResponseDTO;
import com.app.YouthCompass.policy.dto.RecommendedPolicyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 추천 화면에 필요한 결과를 만드는 서비스
// AI 조건 추출(openAiService) → 회원정보로 빈칸 채움 → 규칙 기반 자격 판정·정렬(PolicyRecommendationService)
// → 상위 후보를 AI가 개인 맞춤으로 다시 골라 추천 이유·기대 효과 생성(PolicyInsightService) → 상위 10개 → DTO
@Service
@RequiredArgsConstructor
public class RecommendResultService {

    // 화면에 보여줄 추천 정책 개수
    private static final int TOP_N = 10;

    // AI 맞춤 재정렬에 넘기는 후보 수 (규칙 기반 상위 N개)
    private static final int CANDIDATE_POOL = 30;

    // AI가 실패했을 때 지원내용에서 보여줄 길이
    private static final int FALLBACK_EFFECT_LIMIT = 120;

    private final openAiService aiService;
    private final PolicyRecommendationService policyRecommendationService;
    private final RecommendReasonBuilder reasonBuilder;
    private final PolicyInsightService insightService;
    private final MemberProfileDefaults memberProfileDefaults;

    // 사용자 문장으로 추천 (AI 호출 O)
    // member: 로그인 회원 (없으면 null) → 문장에 없는 나이·지역을 회원정보로 채움
    public RecommendResponseDTO recommendFromSentence(String sentence, MemberVO member) {

        openAiService.ExtractionResult extracted = aiService.extract(sentence);

        UserPolicyProfileVO profile = extracted.profile();
        List<String> memberFilled = memberProfileDefaults.fill(profile, member);

        return recommend(sentence, profile, extracted.intent(), memberFilled);
    }

    // 사용자가 화면에서 직접 고친 조건으로 추천 (조건 추출 AI 호출 X)
    // 회원정보로 다시 채우지 않음 → 사용자가 일부러 비운 칸을 존중
    public RecommendResponseDTO recommendFromProfile(
            String sentence,
            UserPolicyProfileVO profile,
            PolicySearchIntent intent
    ) {
        return recommend(sentence, profile, intent, List.of());
    }

    private RecommendResponseDTO recommend(
            String sentence,
            UserPolicyProfileVO profile,
            PolicySearchIntent intent,
            List<String> memberFilled
    ) {

        // 1. 규칙 기반: 자격 통과 + 관련도 순 정렬 → 상위 후보
        List<PolicyEvaluationResult> candidates =
                policyRecommendationService.recommend(profile, intent)
                        .stream()
                        .limit(CANDIDATE_POOL)
                        .toList();

        // 2. 후보마다 충족 조건 / 확인 필요 조건 문장 (화면에도 쓰고 AI에도 근거로 넘김)
        Map<Long, List<String>> matched = new HashMap<>();
        Map<Long, List<String>> unchecked = new HashMap<>();
        Map<Long, PolicyEvaluationResult> byId = new LinkedHashMap<>();

        for (PolicyEvaluationResult result : candidates) {
            Long id = result.getPolicy().getPolicyId();
            byId.put(id, result);
            matched.put(id, reasonBuilder.reasons(result, profile, intent));
            unchecked.put(id, reasonBuilder.unknownReasons(result, profile));
        }

        // 3. AI 개인 맞춤 재정렬 + 추천 이유 + 기대 효과 (실패하면 빈 목록)
        List<PolicyInsightService.Insight> insights =
                insightService.pick(sentence, profile, intent, candidates, matched, unchecked, TOP_N);

        // 4. 최종 목록: AI가 고른 순서 → AI 결과가 없으면 규칙 기반 순서
        List<RecommendedPolicyDTO> policies = new ArrayList<>();

        for (PolicyInsightService.Insight insight : insights) {
            PolicyEvaluationResult result = byId.get(insight.policyId());
            policies.add(toDto(result, matched, unchecked, insight.reason(), insight.effect(), true));
        }

        if (policies.isEmpty()) {
            for (PolicyEvaluationResult result : candidates) {
                if (policies.size() >= TOP_N) {
                    break;
                }
                policies.add(toDto(result, matched, unchecked, null, null, false));
            }
        }

        return new RecommendResponseDTO(profile, intent, policies, memberFilled, !insights.isEmpty());
    }

    // 판정 결과 → 카드 DTO
    // AI 문장이 비어 있으면 규칙 기반 문장으로 채움
    private RecommendedPolicyDTO toDto(
            PolicyEvaluationResult result,
            Map<Long, List<String>> matched,
            Map<Long, List<String>> unchecked,
            String aiReason,
            String aiEffect,
            boolean personalized
    ) {

        Long id = result.getPolicy().getPolicyId();

        RecommendedPolicyDTO dto = RecommendedPolicyDTO.from(result);
        dto.setReasons(matched.getOrDefault(id, List.of()));
        dto.setUnknownReasons(unchecked.getOrDefault(id, List.of()));
        dto.setRecommendReason(aiReason != null ? aiReason : fallbackReason(dto.getReasons()));
        dto.setExpectedEffect(aiEffect != null ? aiEffect : fallbackEffect(result.getPolicy()));
        dto.setPersonalized(personalized);

        return dto;
    }

    // AI 없이 만드는 추천 이유: 규칙 문장 앞의 두 개를 이어 붙임
    private String fallbackReason(List<String> reasons) {

        if (reasons == null || reasons.isEmpty()) {
            return "입력한 조건으로 신청 자격에서 걸리는 부분이 없는 정책이에요.";
        }
        return String.join(" · ", reasons.subList(0, Math.min(2, reasons.size())));
    }

    // AI 없이 만드는 기대 효과: 지원내용 앞부분
    private String fallbackEffect(PolicyVO policy) {

        String support = policy.getPolicySupportContent();

        if (support == null || support.isBlank()) {
            return "지원 내용은 공고에서 확인해 주세요.";
        }

        String oneLine = support.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= FALLBACK_EFFECT_LIMIT
                ? oneLine
                : oneLine.substring(0, FALLBACK_EFFECT_LIMIT) + "…";
    }
}
