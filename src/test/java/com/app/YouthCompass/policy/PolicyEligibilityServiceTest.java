package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.*;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.recommendation.PolicyRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

@SpringBootTest
class PolicyEligibilityServiceTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRecommendationService policyRecommendationService;

    @Test
    void 정책_추천_후보_조회() {

        // =========================
        // 1. 사용자 조건
        // =========================
        UserPolicyProfileVO user = UserPolicyProfileVO.builder()
                .age(25)
                .region(Region.SEOUL)
                .school(SchoolRequirement.UNIVERSITY_STUDENT)
                .job(JobRequirement.UNEMPLOYED)
                .marriage(MarriageRequirement.SINGLE)
                .annualIncome(2400L)
                .major(MajorRequirement.ENGINEERING)
                .specialTargets(Set.of())
                .build();


        // =========================
        // 2. 사용자가 원하는 정책
        // "주거 분야에서 월세 관련 정책"
        // =========================
        PolicySearchIntent intent = PolicySearchIntent.builder()
                .category("일자리")
                .build();


        // =========================
        // 3. 추천 실행
        // =========================
        List<PolicyEvaluationResult> candidates =
                policyRecommendationService.recommend(user, intent);


        // =========================
        // 4. 기본 결과
        // =========================
        System.out.println("\n==============================");
        System.out.println("사용자 지역 = " + user.getRegion());
        System.out.println("전체 정책 = " + policyService.findAllPolicies().size());
        System.out.println("1차 필터 후 후보 = " + candidates.size());
        System.out.println("==============================\n");


        // =========================
        // 5. TOP 10 출력
        // =========================
        int limit = Math.min(10, candidates.size());

        for (int i = 0; i < limit; i++) {
            PolicyEvaluationResult result = candidates.get(i);
            System.out.println(
                    (i + 1) + "위"
                            + " | ID=" + result.getPolicy().getPolicyExternalId()
                            + " | 관련도=" + result.getRelevanceScore()
                            + " | 자격점수=" + result.getSpecificMatchScore()
                            + " | UNKNOWN=" + result.getUnknownCount()
                            + " | 지역판정=" + result.getRegionResult()
//                            + " | 지역코드=" + result.getPolicy().getPolicyRegionCodes()
                            + " | 대분류=" + result.getPolicy().getPolicyLargeCategory()
                            + " | 중분류=" + result.getPolicy().getPolicyMediumCategory()
                            + " | 키워드=" + result.getPolicy().getPolicyKeywords()
                            + " | 기관=" + result.getPolicy().getPolicyOrganizationName()
                            + " | 정책명=" + result.getPolicy().getPolicyName()
            );
        }
    }
}