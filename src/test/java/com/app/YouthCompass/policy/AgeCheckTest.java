package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgeCheckTest {

    private final PolicyEligibilityService service = new PolicyEligibilityService();
    private final UserPolicyProfileVO user25 =
            UserPolicyProfileVO.builder().age(25).build();

    private PolicyVO policy(Integer min, Integer max) {
        return PolicyVO.builder().policyMinAge(min).policyMaxAge(max).build();
    }

    @Test
    void 나이_0_0은_제한없음_MATCH() {
        assertThat(service.checkAge(user25, policy(0, 0)))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 최소만_있고_최대_0이면_최소만_검사() {
        assertThat(service.checkAge(user25, policy(19, 0)))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 범위_19_24는_25세_NOT_MATCH() {
        assertThat(service.checkAge(user25, policy(19, 24)))
                .isEqualTo(EligibilityStatus.NOT_MATCH);
    }

    @Test
    void 경계값_19_25는_25세_MATCH() {
        assertThat(service.checkAge(user25, policy(19, 25)))
                .isEqualTo(EligibilityStatus.MATCH);
    }
}