package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeCheckTest {

    private final PolicyEligibilityService service = new PolicyEligibilityService();

    // 연봉 2400만원 사용자 (만원 단위)
    private final UserPolicyProfileVO user =
            UserPolicyProfileVO.builder().annualIncome(2400L).build();

    private PolicyVO policyWithMax(long maxManwon) {
        return PolicyVO.builder()
                .policyIncomeConditionCode("0043002")   // 연소득 조건
                .policyIncomeMaxAmount(maxManwon)
                .build();
    }

    @Test
    void 상한_3000만원_정책은_MATCH() {
        assertThat(service.checkIncome(user, policyWithMax(3000)))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 상한_2000만원_정책은_NOT_MATCH() {
        assertThat(service.checkIncome(user, policyWithMax(2000)))
                .isEqualTo(EligibilityStatus.NOT_MATCH);
    }
    private PolicyVO policyWithRange(Long min, Long max) {
        return PolicyVO.builder()
                .policyIncomeConditionCode("0043002")
                .policyIncomeMinAmount(min)
                .policyIncomeMaxAmount(max)
                .build();
    }

    @Test
    void 금액_0_0이면_UNKNOWN() {
        assertThat(service.checkIncome(user, policyWithRange(0L, 0L)))
                .isEqualTo(EligibilityStatus.UNKNOWN);
    }

    @Test
    void 금액_null_null이면_UNKNOWN() {
        assertThat(service.checkIncome(user, policyWithRange(null, null)))
                .isEqualTo(EligibilityStatus.UNKNOWN);
    }

    @Test
    void 최소_0_최대_3000이면_최대만_검사해서_MATCH() {
        assertThat(service.checkIncome(user, policyWithRange(0L, 3000L)))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 사용자_소득_모르면_UNKNOWN() {
        UserPolicyProfileVO noIncome = UserPolicyProfileVO.builder().build();
        assertThat(service.checkIncome(noIncome, policyWithRange(0L, 3000L)))
                .isEqualTo(EligibilityStatus.UNKNOWN);
    }
}