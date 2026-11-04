package com.app.YouthCompass.history;

import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.dto.RecommendResponseDTO;
import com.app.YouthCompass.policy.dto.RecommendedPolicyDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // 테스트마다 롤백 → 로컬 DB에 흔적 안 남음
class RecommendHistoryServiceTest {

    @Autowired
    private RecommendHistoryService historyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // 저장된 값 확인용

    private static final Long MEMBER_ID = 999999L;

    private RecommendedPolicyDTO policy(Long policyId, String reason) {
        return RecommendedPolicyDTO.builder()
                .policyId(policyId)
                .policyName("테스트 정책 " + policyId)
                .recommendReason(reason)
                .expectedEffect("월세 부담이 줄어요")
                .build();
    }

    private RecommendResponseDTO result(List<RecommendedPolicyDTO> policies) {
        RecommendResponseDTO dto = new RecommendResponseDTO();
        dto.setProfile(UserPolicyProfileVO.builder()
                .age(25)
                .region(Region.GYEONGGI)
                .annualIncome(2400L)
                .build());
        dto.setIntent(PolicySearchIntent.builder().category("주거").keyword("월세").build());
        dto.setPolicies(policies);
        return dto;
    }

    @Test
    void 추천_결과를_기록과_순위별_항목으로_저장한다() {
        Long historyId = historyService.save(MEMBER_ID, "경기도 사는 25살 월세 지원",
                result(List.of(policy(999999L, "이유 1"), policy(999998L, "이유 2"))));

        assertThat(historyId).isNotNull();

        // 항목: 순위 순서대로 정책 번호가 저장됨
        List<Long> policyIds = jdbcTemplate.queryForList(
                "SELECT POLICY_ID FROM TBL_RECOMMEND_ITEM WHERE HISTORY_ID = ? ORDER BY RANK_NO",
                Long.class, historyId);
        assertThat(policyIds).containsExactly(999999L, 999998L);

        // 기록: 문장과 화면 표시용 조건 JSON
        String sentence = jdbcTemplate.queryForObject(
                "SELECT SENTENCE FROM TBL_RECOMMEND_HISTORY WHERE HISTORY_ID = ?", String.class, historyId);
        String profileJson = jdbcTemplate.queryForObject(
                "SELECT PROFILE_JSON FROM TBL_RECOMMEND_HISTORY WHERE HISTORY_ID = ?", String.class, historyId);

        assertThat(sentence).isEqualTo("경기도 사는 25살 월세 지원");
        assertThat(profileJson)
                .contains("\"나이\":\"25세\"")
                .contains("\"지역\":\"경기도\"")
                .contains("\"연소득\":\"2,400만원\"")
                .contains("\"찾는 지원\":\"월세\"");
    }

    @Test
    void 긴_추천_이유는_500자로_잘라서_저장한다() {
        Long historyId = historyService.save(MEMBER_ID, "문장",
                result(List.of(policy(999999L, "가".repeat(600)))));

        String reason = jdbcTemplate.queryForObject(
                "SELECT REASON FROM TBL_RECOMMEND_ITEM WHERE HISTORY_ID = ?", String.class, historyId);

        assertThat(reason).hasSize(500).endsWith("…");
    }

    @Test
    void 비로그인이거나_추천_결과가_없으면_저장하지_않는다() {
        assertThat(historyService.save(null, "문장", result(List.of(policy(999999L, "이유"))))).isNull();
        assertThat(historyService.save(MEMBER_ID, "문장", result(List.of()))).isNull();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TBL_RECOMMEND_HISTORY WHERE MEMBER_ID = ?", Integer.class, MEMBER_ID);
        assertThat(count).isZero();
    }

    @Test
    void 비로그인이면_기록_목록은_빈_리스트() {
        assertThat(historyService.findHistories(null)).isEmpty();
    }
}