package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.ConditionRequestDTO;
import com.app.YouthCompass.policy.dto.PolicyDetailResponseDTO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyBrowseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyEligibilityService eligibilityService;

    // 찜 버튼 작업 때 필터 선택지가 빠져서 분야·지역 select가 비어 있던 문제 재발 방지
    @Test
    void 정책_목록에_분야와_지역_선택지가_나온다() throws Exception {
        mockMvc.perform(get("/policies"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryOptions", "regionOptions"))
                .andExpect(content().string(containsString("<option value=\"주거\">주거</option>")))
                .andExpect(content().string(containsString("<option value=\"SEOUL\">서울특별시</option>")));
    }

    // 추천 카드 → 상세: 회원정보가 아니라 추천 때 조건으로 판정 (추천에선 지역 충족인데 상세에선 미충족으로 나오던 문제)
    @Test
    void 추천에서_온_상세는_추천_때_조건으로_판정한다() throws Exception {

        ConditionRequestDTO conditions = new ConditionRequestDTO();
        conditions.setAge(31);
        conditions.setRegion(Region.INCHEON);
        conditions.setSigungu("연수구");
        PolicyVO incheonOnly = regionLimitedPolicy(conditions.toProfile());

        // 로그인하지 않아도 추천 조건이 있으면 판정
        PolicyDetailResponseDTO detail = (PolicyDetailResponseDTO) mockMvc
                .perform(get("/policies/" + incheonOnly.getPolicyId() + conditions.toDetailQuery()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("추천에서 쓴 조건")))
                .andReturn().getModelAndView().getModel().get("detail");

        assertThat(detail.isEvaluated()).isTrue();
        assertThat(detail.isFromRecommend()).isTrue();
        assertThat(detail.getConditions())
                .filteredOn(row -> row.name().equals("지역"))
                .extracting(PolicyDetailResponseDTO.ConditionRow::status)
                .containsExactly(EligibilityStatus.MATCH);
    }

    @Test
    void 추천_조건이_없거나_잘못되면_예전처럼_판정하지_않는다() throws Exception {

        PolicyVO any = policyService.findOpenPolicies().get(0);

        for (String url : new String[]{
                "/policies/" + any.getPolicyId(),
                "/policies/" + any.getPolicyId() + "?from=recommend&region=없는지역"}) {

            PolicyDetailResponseDTO detail = (PolicyDetailResponseDTO) mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andReturn().getModelAndView().getModel().get("detail");

            // 비로그인 + 추천 조건 없음 → 판정 없이 정책 정보만
            assertThat(detail.isEvaluated()).isFalse();
            assertThat(detail.isFromRecommend()).isFalse();
        }
    }

    @Test
    void 상세_링크_쿼리는_빈_칸을_빼고_한글_시군을_인코딩한다() {

        ConditionRequestDTO conditions = new ConditionRequestDTO();
        conditions.setSentence("인천 연수구 사는 31살");
        conditions.setAge(31);
        conditions.setRegion(Region.INCHEON);
        conditions.setSigungu("연수구");
        conditions.setCategory("주거");

        assertThat(conditions.toDetailQuery())
                .isEqualTo("?from=recommend&age=31&region=INCHEON&sigungu=%EC%97%B0%EC%88%98%EA%B5%AC");
    }

    // 사용자 지역에선 지역 조건 충족, 지역을 모르면 확인 필요인 정책 (지역 제한이 실제로 있는 것)
    private PolicyVO regionLimitedPolicy(UserPolicyProfileVO profile) {
        UserPolicyProfileVO unknown = new UserPolicyProfileVO();
        return policyService.findOpenPolicies().stream()
                .filter(p -> eligibilityService.checkRegion(profile, p) == EligibilityStatus.MATCH)
                .filter(p -> eligibilityService.checkRegion(unknown, p) == EligibilityStatus.UNKNOWN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("인천 지역 정책이 없어요. 동기화 후 실행하세요."));
    }
}
