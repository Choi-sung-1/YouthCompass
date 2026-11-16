package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.policy.PolicyService;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import com.app.YouthCompass.policy.domain.recommendation.PolicyCategories;
import com.app.YouthCompass.policy.domain.recommendation.PolicyKeywords;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PolicyRecommendationService {

    private final PolicyService policyService;
    private final PolicyEligibilityService policyEligibilityService;


   public List<PolicyEvaluationResult> recommend(UserPolicyProfileVO user,PolicySearchIntent intent) {

//       DB 전체 정책 조회
       List<PolicyVO> policies = policyService.findOpenPolicies();
//       추천 후보 저장
       List<PolicyEvaluationResult> candidates = new ArrayList<>();

       for (PolicyVO policy : policies) {

           PolicyEvaluationResult result =
                   policyEligibilityService.evaluate(user, policy);

           if (result.getNotMatchCount() == 0) {

               int relevanceScore =
                       calculateRelevanceScore(policy, intent);

               result.setRelevanceScore(relevanceScore);

               candidates.add(result);
           }
       }
       candidates.sort(
               java.util.Comparator
                       // 1. 검색 내용과 관련도 높은 정책 우선
                       .comparingInt(PolicyEvaluationResult::getRelevanceScore)
                       .reversed()

                       // 2. UNKNOWN이 적은 정책 우선
                       .thenComparingInt(
                               PolicyEvaluationResult::getUnknownCount
                       )

                       // 3. 자격조건 점수가 높은 정책 우선
                       .thenComparing(
                               java.util.Comparator
                                       .comparingInt(
                                               PolicyEvaluationResult::getSpecificMatchScore
                                       )
                                       .reversed()
                       )
       );

       return candidates;
   }
    private int calculateRelevanceScore(
            PolicyVO policy,
            PolicySearchIntent intent
    ) {

        if (intent == null) {
            return 0;
        }

        int score = 0;

        String category = intent.getCategory();
        String keyword = intent.getKeyword();

        // =========================
        // 1. 대분류
        // =========================
        if (PolicyCategories.matches(category, policy.getPolicyLargeCategory())) {

            score += 3;
        }

        // =========================
        // 2. 중분류
        // =========================
        if (keyword != null
                && policy.getPolicyMediumCategory() != null
                && policy.getPolicyMediumCategory().contains(keyword)) {

            score += 2;
        }

        // =========================
        // 3. 정책 키워드
        // =========================
        if (keyword != null
                && policy.getPolicyKeywords() != null
                && policy.getPolicyKeywords().contains(keyword)) {

            score += 2;
        }

        // =========================
        // 4. 정책명
        // =========================
        if (keyword != null
                && policy.getPolicyName() != null
                && policy.getPolicyName().contains(keyword)) {

            score += 3;
        }

        // =========================
        // 5. 정책 설명·지원 내용 (비슷한 말 포함)
        // =========================
        // 없으면 같은 분야 정책이 전부 동점이라 찾는 지원과 관계없는 정책이 앞에 옴
        // ex) "목돈" → 분야 점수만 같은 상담·예술 지원이 저축 정책보다 앞에 옴
        // 가장 약한 근거라 1점 — 같은 분야 정책 사이 순서를 정하는 정도
        if (PolicyKeywords.mentionedInText(policy, keyword)) {

            score += 1;
        }

        return score;
    }
}













