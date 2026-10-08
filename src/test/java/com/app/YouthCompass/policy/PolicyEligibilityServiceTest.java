package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyEligibilityServiceImplTest {

    private final PolicyEligibilityService service =
            new PolicyEligibilityService();

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
    @Test
    void 사용자지역과_정책지역이_같으면_MATCH() {

        UserPolicyProfileVO user =
                UserPolicyProfileVO.builder()
                        .region(Region.GYEONGGI)
                        .build();

        PolicyVO policy =
                PolicyVO.builder()
                        .policyRegionCodes("41")
                        .build();

        EligibilityStatus result =
                service.checkRegion(user, policy);

        assertEquals(
                EligibilityStatus.MATCH,
                result
        );
    }


    @Test
    void 사용자지역과_정책지역이_다르면_NOT_MATCH() {

        UserPolicyProfileVO user =
                UserPolicyProfileVO.builder()
                        .region(Region.GYEONGGI)
                        .build();

        PolicyVO policy =
                PolicyVO.builder()
                        .policyRegionCodes("11")
                        .build();

        EligibilityStatus result =
                service.checkRegion(user, policy);

        assertEquals(
                EligibilityStatus.NOT_MATCH,
                result
        );
    }


    @Test
    void 여러지역중_사용자지역이_있으면_MATCH() {

        UserPolicyProfileVO user =
                UserPolicyProfileVO.builder()
                        .region(Region.GYEONGGI)
                        .build();

        PolicyVO policy =
                PolicyVO.builder()
                        .policyRegionCodes("11,41,28")
                        .build();

        EligibilityStatus result =
                service.checkRegion(user, policy);

        assertEquals(
                EligibilityStatus.MATCH,
                result
        );
    }


    @Test
    void 사용자지역정보가_없으면_UNKNOWN() {

        UserPolicyProfileVO user =
                UserPolicyProfileVO.builder()
                        .build();

        PolicyVO policy =
                PolicyVO.builder()
                        .policyRegionCodes("41")
                        .build();

        EligibilityStatus result =
                service.checkRegion(user, policy);

        assertEquals(
                EligibilityStatus.UNKNOWN,
                result
        );
    }


    @Test
    void 정책에_지역제한이_없으면_MATCH() {

        UserPolicyProfileVO user =
                UserPolicyProfileVO.builder()
                        .region(Region.GYEONGGI)
                        .build();

        PolicyVO policy =
                PolicyVO.builder()
                        .build();

        EligibilityStatus result =
                service.checkRegion(user, policy);

        assertEquals(
                EligibilityStatus.MATCH,
                result
        );
    }
}