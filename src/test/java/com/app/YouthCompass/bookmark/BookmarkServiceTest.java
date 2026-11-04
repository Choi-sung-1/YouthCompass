package com.app.YouthCompass.bookmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional  // 테스트마다 롤백 → 로컬 DB에 흔적 안 남음
class BookmarkServiceTest {

    @Autowired
    private BookmarkService bookmarkService;

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
}