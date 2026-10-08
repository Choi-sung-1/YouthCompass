package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.policy.PolicyService;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PolicyRecommendationService {

    private final PolicyService policyService;
    private final PolicyEligibilityService policyEligibilityService;


   public List<PolicyEvaluationResult> recommend(UserPolicyProfileVO user){

//       DB 전체 정책 조회
       List<PolicyVO> policies = policyService.findAllPolicies();
//       추천 후보 저장
       List<PolicyEvaluationResult> candidates = new ArrayList<>();

       for (PolicyVO policy : policies){
           PolicyEvaluationResult result = policyEligibilityService.evaluate(user,policy);

//           명확한 부적격 조건이 하나도 없는 정책만 후보에 추가
           if (result.getNotMatchCount() == 0){
               candidates.add(result);
           }
       }
       return candidates;
   }
}













