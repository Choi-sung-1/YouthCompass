package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.policy.PolicyService;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
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
       List<PolicyVO> policies = policyService.findAllPolicies();
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
        if (category != null
                && policy.getPolicyLargeCategory() != null
                && policy.getPolicyLargeCategory().contains(category)) {

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

        return score;
    }
}













