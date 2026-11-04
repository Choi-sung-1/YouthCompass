package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.dto.MissedPolicyDTO;
import com.app.YouthCompass.policy.eligibility.MissedPolicyFinder;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 로컬 H2에 동기화된 정책이 있어야 의미 있는 테스트 (읽기만 함)
@SpringBootTest
class MissedPolicyFinderTest {

    @Autowired
    MissedPolicyFinder finder;

    @Autowired
    PolicyEligibilityService eligibilityService;

    @Autowired
    PolicyMapper policyMapper;

    // 만 35세, 경기 평택, 연소득 5,000만원 → 나이·소득 때문에 놓치는 정책이 꽤 있을 조건
    private UserPolicyProfileVO profile() {
        return UserPolicyProfileVO.builder()
                .age(35)
                .region(Region.GYEONGGI)
                .sigunguCode("41220")
                .annualIncome(5000L)
                .build();
    }

    @Test
    void 놓친_정책은_조건이_정확히_하나만_안_맞고_안내_문구가_있다() {

        List<MissedPolicyDTO> missed = finder.find(profile(), null, 10);

        assertThat(missed).hasSizeLessThanOrEqualTo(10);
        for (MissedPolicyDTO m : missed) {
            PolicyEvaluationResult r = eligibilityService.evaluate(profile(), policyMapper.findPolicyById(m.getPolicyId()));
            assertThat(r.getNotMatchCount()).isEqualTo(1);
            assertThat(m.getMissedCondition()).isNotBlank();
            assertThat(m.getHint()).isNotBlank();
        }
    }

    @Test
    void 찾는_분야가_있으면_그_분야_정책만_나온다() {

        PolicySearchIntent intent = PolicySearchIntent.builder().category("주거").build();

        List<MissedPolicyDTO> missed = finder.find(profile(), intent, 10);

        for (MissedPolicyDTO m : missed) {
            assertThat(policyMapper.findPolicyById(m.getPolicyId()).getPolicyLargeCategory()).contains("주거");
        }
    }

    @Test
    void 프로필이_없거나_개수가_0이면_빈_목록() {

        assertThat(finder.find(null, null, 3)).isEmpty();
        assertThat(finder.find(profile(), null, 0)).isEmpty();
    }
}