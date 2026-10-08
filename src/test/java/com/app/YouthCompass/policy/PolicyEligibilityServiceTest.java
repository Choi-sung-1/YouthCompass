package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.*;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
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
    private PolicyEligibilityService policyEligibilityService;
    @Autowired
    private PolicyRecommendationService policyRecommendationService;

    @Test
    void 정책_추천_후보_조회() {

        UserPolicyProfileVO user = UserPolicyProfileVO.builder()
                .age(25)
                .region(Region.GANGWON)
                .school(SchoolRequirement.UNIVERSITY_STUDENT)
                .job(JobRequirement.UNEMPLOYED)
                .marriage(MarriageRequirement.SINGLE)
                .annualIncome(2400L)
                .major(MajorRequirement.ENGINEERING)
                .specialTargets(Set.of())
                .build();

        List<PolicyEvaluationResult> candidates =policyRecommendationService.recommend(user);

        System.out.println("전체 정책 = " + policyService.findAllPolicies().size());

        System.out.println("1차 필터 후 후보 = " + candidates.size());

        for (PolicyEvaluationResult result : candidates) {
            System.out.println("자격요건 정확도 점수:"+ result.getSpecificMatchScore());
            System.out.println(
                    result.getPolicy().getPolicyName()
                            + " | MATCH=" + result.getMatchCount()
                            + " | UNKNOWN=" + result.getUnknownCount()
                            + " | NOT_MATCH=" + result.getNotMatchCount()
            );
        }
    }
}