package com.app.YouthCompass.bookmark;

import com.app.YouthCompass.policy.dto.PolicyListItemDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional  // 테스트마다 롤백 → 로컬 DB에 흔적 안 남음
class BookmarkServiceTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // 테스트용 정책 한 건을 넣기 위해 사용 (롤백됨)

    private static final Long MEMBER_ID = 999999L;
    private static final Long POLICY_ID = 999999L;
    private static final Long OTHER_POLICY_ID = 999998L;

    @Test
    void 처음_토글하면_찜되고_다시_토글하면_취소된다() {
        assertThat(bookmarkService.toggleBookmark(MEMBER_ID, POLICY_ID)).isTrue();
        assertThat(bookmarkService.isBookmarked(MEMBER_ID, POLICY_ID)).isTrue();

        assertThat(bookmarkService.toggleBookmark(MEMBER_ID, POLICY_ID)).isFalse();
        assertThat(bookmarkService.isBookmarked(MEMBER_ID, POLICY_ID)).isFalse();
    }

    @Test
    void 찜한_정책_ID를_한번에_조회한다() {
        bookmarkService.toggleBookmark(MEMBER_ID, POLICY_ID);
        bookmarkService.toggleBookmark(MEMBER_ID, OTHER_POLICY_ID);

        assertThat(bookmarkService.findBookmarkedPolicyIds(MEMBER_ID))
                .containsExactlyInAnyOrder(POLICY_ID, OTHER_POLICY_ID);
    }

    @Test
    void 비로그인이면_찜_아님_빈_Set() {
        assertThat(bookmarkService.isBookmarked(null, POLICY_ID)).isFalse();
        assertThat(bookmarkService.findBookmarkedPolicyIds(null)).isEmpty();
    }

    @Test
    void 회원_ID_없이_토글하면_예외() {
        assertThatThrownBy(() -> bookmarkService.toggleBookmark(null, POLICY_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 찜_목록은_정책정보와_D_day를_담고_없는_정책은_빠진다() {
        // 테스트용 정책: 특정기간(0057001), 마감 3일 뒤
        LocalDate endDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3);
        jdbcTemplate.update("""
                INSERT INTO TBL_POLICY (POLICY_SOURCE, POLICY_EXTERNAL_ID, POLICY_NAME,
                                        POLICY_APPLY_PERIOD_CODE, POLICY_APPLY_END_DATE)
                VALUES ('TEST', 'BOOKMARK-TEST-1', '찜 테스트 정책', '0057001', ?)
                """, endDate);
        Long testPolicyId = jdbcTemplate.queryForObject(
                "SELECT POLICY_ID FROM TBL_POLICY WHERE POLICY_EXTERNAL_ID = 'BOOKMARK-TEST-1'",
                Long.class);

        bookmarkService.toggleBookmark(MEMBER_ID, testPolicyId);  // 실제 있는 정책
        bookmarkService.toggleBookmark(MEMBER_ID, POLICY_ID);     // 999999: TBL_POLICY에 없는 정책

        List<PolicyListItemDTO> list = bookmarkService.findBookmarkedPolicies(MEMBER_ID);

        assertThat(list).hasSize(1);  // 없는 정책은 JOIN에서 빠짐
        assertThat(list.get(0).getPolicyName()).isEqualTo("찜 테스트 정책");
        assertThat(list.get(0).getDeadlineLabel()).isEqualTo("D-3");
        assertThat(list.get(0).isClosingSoon()).isTrue();
    }

    @Test
    void 비로그인이면_찜_목록은_빈_리스트() {
        assertThat(bookmarkService.findBookmarkedPolicies(null)).isEmpty();
    }
}