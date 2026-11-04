package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.ConditionRequestDTO;
import com.app.YouthCompass.policy.dto.RecheckQuestionDTO;
import com.app.YouthCompass.policy.dto.RecheckRequestDTO;
import com.app.YouthCompass.policy.dto.RecheckResponseDTO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import com.app.YouthCompass.policy.eligibility.RecheckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

// 로컬 H2에 동기화된 정책이 있어야 의미 있는 테스트 (읽기만 함)
@SpringBootTest
class RecheckServiceTest {

    @Autowired
    RecheckService recheckService;

    @Autowired
    PolicyService policyService;

    @Autowired
    PolicyEligibilityService eligibilityService;

    // 학력 조건이 실제로 있는 신청 가능 정책 (학력을 모르면 확인 필요가 되는 것)
    private PolicyVO policyWithSchoolCondition() {
        UserPolicyProfileVO empty = new UserPolicyProfileVO();
        return policyService.findOpenPolicies().stream()
                .filter(p -> eligibilityService.evaluate(empty, p).getSchoolResult() == EligibilityStatus.UNKNOWN)
                .filter(p -> firstSchool(p) != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("학력 조건이 있는 정책이 없어요. 동기화 후 실행하세요."));
    }

    // 정책이 요구하는 학력 중 첫 번째
    private SchoolRequirement firstSchool(PolicyVO p) {
        if (p.getPolicySchoolCodes() == null) {
            return null;
        }
        return Arrays.stream(p.getPolicySchoolCodes().split(","))
                .map(String::trim)
                .map(SchoolRequirement::fromCode)
                .filter(s -> s != SchoolRequirement.NO_RESTRICTION && s != SchoolRequirement.UNKNOWN)
                .findFirst()
                .orElse(null);
    }

    private RecheckRequestDTO request(Long policyId, ConditionRequestDTO conditions) {
        RecheckRequestDTO request = new RecheckRequestDTO();
        request.setPolicyId(policyId);
        request.setConditions(conditions);
        return request;
    }

    @Test
    void 학력을_모르면_학력_질문이_나온다() {

        PolicyVO policy = policyWithSchoolCondition();

        RecheckResponseDTO response = recheckService
                .recheck(request(policy.getPolicyId(), new ConditionRequestDTO()))
                .orElseThrow();

        assertThat(response.questions()).extracting(RecheckQuestionDTO::field).contains("school");
    }

    @Test
    void 학력을_답하면_학력_질문과_학력_확인필요가_사라진다() {

        PolicyVO policy = policyWithSchoolCondition();
        ConditionRequestDTO conditions = new ConditionRequestDTO();
        conditions.setSchool(firstSchool(policy));

        RecheckResponseDTO response = recheckService
                .recheck(request(policy.getPolicyId(), conditions))
                .orElseThrow();

        assertThat(response.questions()).extracting(RecheckQuestionDTO::field).doesNotContain("school");
        assertThat(response.unknownReasons()).noneMatch(reason -> reason.contains("학력"));
    }

    @Test
    void 없는_정책이면_빈_결과() {

        assertThat(recheckService.recheck(request(-1L, new ConditionRequestDTO()))).isEmpty();
    }
}