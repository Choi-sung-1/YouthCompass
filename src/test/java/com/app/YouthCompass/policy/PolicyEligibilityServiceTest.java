package com.app.YouthCompass.policy;

import com.app.YouthCompass.domain.vo.member.UserPolicyProfileVO;
import com.app.YouthCompass.domain.vo.policy.EligibilityStatus;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import com.app.YouthCompass.service.policy.PolicyEligibilityService;
import com.app.YouthCompass.service.policy.PolicyEligibilityServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyEligibilityServiceImplTest {

    private final PolicyEligibilityService service =
            new PolicyEligibilityServiceImpl();

//    나이 조건이 충족하면 MATCH
    @Test
    void age_Match() {

        UserPolicyProfileVO user = UserPolicyProfileVO.builder().age(25).build();
        PolicyVO policy = PolicyVO.builder().policyMinAge(19).policyMaxAge(34).build();
        EligibilityStatus result = service.checkAge(user, policy);

        assertEquals(EligibilityStatus.MATCH, result);
        System.out.println(result.toString());
        
    }

    //    최소 나이보다 어리면 NOT MATCH
    @Test
    void minAge_Match() {
        UserPolicyProfileVO user = UserPolicyProfileVO.builder().age(18).build();
        PolicyVO policy = PolicyVO.builder().policyMinAge(19).policyMaxAge(34).build();
        EligibilityStatus result = service.checkAge(user, policy);

        assertEquals(EligibilityStatus.NOT_MATCH, result);
        System.out.println(result.toString());

    }
    //    사용자 나이 미입력 시 UNKNOWN
    @Test
    void noneAgeMatch() {
        UserPolicyProfileVO user = UserPolicyProfileVO.builder().build();
        PolicyVO policy = PolicyVO.builder().policyMinAge(19).policyMaxAge(34).build();
        EligibilityStatus result = service.checkAge(user, policy);

        assertEquals(EligibilityStatus.UNKNOWN, result);
        System.out.println(result.toString());
    }
}