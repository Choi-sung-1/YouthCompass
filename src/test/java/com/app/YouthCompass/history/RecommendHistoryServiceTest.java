package com.app.YouthCompass.history;

import com.app.YouthCompass.history.dto.RecommendHistoryDetailDTO;
import com.app.YouthCompass.history.dto.RecommendHistoryItemDTO;
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

import java.time.LocalDate;
import java.time.ZoneId;
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

    @Test
    void 기록_상세는_조건과_순위별_카드를_담고_없는_정책도_자리를_남긴다() {
        // 테스트용 정책: 특정기간(0057001), 마감 3일 뒤
        LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3);
        jdbcTemplate.update("""
                INSERT INTO TBL_POLICY (POLICY_SOURCE, POLICY_EXTERNAL_ID, POLICY_NAME, POLICY_ORGANIZATION_NAME,
                                        POLICY_APPLY_PERIOD_CODE, POLICY_APPLY_END_DATE)
                VALUES ('TEST', 'HISTORY-TEST-1', '기록 테스트 정책', '테스트 기관', '0057001', ?)
                """, endDate);
        Long testPolicyId = jdbcTemplate.queryForObject(
                "SELECT POLICY_ID FROM TBL_POLICY WHERE POLICY_EXTERNAL_ID = 'HISTORY-TEST-1'", Long.class);

        // 1위: 실제 있는 정책, 2위: TBL_POLICY에 없는 정책
        Long historyId = historyService.save(MEMBER_ID, "경기도 사는 25살 월세 지원",
                result(List.of(policy(testPolicyId, "이유 1"), policy(999999L, "이유 2"))));

        RecommendHistoryDetailDTO detail = historyService.findHistoryDetail(historyId, MEMBER_ID).orElseThrow();

        // 조건: 저장 순서 그대로 (나이가 맨 앞)
        assertThat(detail.getSentence()).isEqualTo("경기도 사는 25살 월세 지원");
        assertThat(detail.getConditions()).containsEntry("나이", "25세").containsEntry("지역", "경기도");
        assertThat(detail.getConditions().keySet()).first().isEqualTo("나이");

        // 카드: 순위 순, 1위는 정책 정보 + D-3
        List<RecommendHistoryItemDTO> items = detail.getItems();
        assertThat(items).hasSize(2);

        assertThat(items.get(0).getRankNo()).isEqualTo(1);
        assertThat(items.get(0).getPolicyName()).isEqualTo("기록 테스트 정책");
        assertThat(items.get(0).getPolicyOrganizationName()).isEqualTo("테스트 기관");
        assertThat(items.get(0).getReason()).isEqualTo("이유 1");
        assertThat(items.get(0).getDeadlineLabel()).isEqualTo("D-3");
        assertThat(items.get(0).isClosingSoon()).isTrue();

        // 2위는 정책을 못 찾아도 자리 유지
        assertThat(items.get(1).getRankNo()).isEqualTo(2);
        assertThat(items.get(1).getPolicyId()).isEqualTo(999999L);
        assertThat(items.get(1).getPolicyName()).isNull();
        assertThat(items.get(1).getDeadlineLabel()).isEqualTo("정책 정보 없음");
    }

    @Test
    void 남의_기록_상세는_조회되지_않는다() {
        Long historyId = historyService.save(MEMBER_ID, "내 기록",
                result(List.of(policy(999999L, "이유"))));

        assertThat(historyService.findHistoryDetail(historyId, 999998L)).isEmpty();
        assertThat(historyService.findHistoryDetail(historyId, null)).isEmpty();
    }

    @Test
    void 내_기록을_삭제하면_기록과_항목이_모두_사라진다() {
        Long historyId = historyService.save(MEMBER_ID, "삭제할 기록",
                result(List.of(policy(999999L, "이유 1"), policy(999998L, "이유 2"))));

        assertThat(historyService.delete(historyId, MEMBER_ID)).isTrue();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TBL_RECOMMEND_HISTORY WHERE HISTORY_ID = ?", Integer.class, historyId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TBL_RECOMMEND_ITEM WHERE HISTORY_ID = ?", Integer.class, historyId)).isZero();
        assertThat(historyService.findHistoryDetail(historyId, MEMBER_ID)).isEmpty();
    }

    @Test
    void 남의_기록은_삭제되지_않는다() {
        Long historyId = historyService.save(MEMBER_ID, "남의 기록",
                result(List.of(policy(999999L, "이유 1"))));

        assertThat(historyService.delete(historyId, MEMBER_ID + 1)).isFalse();

        assertThat(historyService.findHistoryDetail(historyId, MEMBER_ID)).isPresent();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TBL_RECOMMEND_ITEM WHERE HISTORY_ID = ?", Integer.class, historyId)).isEqualTo(1);
    }
}
