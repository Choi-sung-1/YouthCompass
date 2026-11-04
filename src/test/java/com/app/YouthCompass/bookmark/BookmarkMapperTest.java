package com.app.YouthCompass.bookmark;

import com.app.YouthCompass.bookmark.domain.BookmarkVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional  // 테스트마다 롤백 → 로컬 DB에 흔적 안 남음
class BookmarkMapperTest {

    @Autowired
    private BookmarkMapper bookmarkMapper;

    private static final Long MEMBER_ID = 999999L;
    private static final Long POLICY_ID = 999999L;

    private BookmarkVO bookmark() {
        BookmarkVO vo = new BookmarkVO();
        vo.setMemberId(MEMBER_ID);
        vo.setPolicyId(POLICY_ID);
        return vo;
    }

    @Test
    void 찜_추가하면_조회된다() {
        assertThat(bookmarkMapper.insertBookmark(bookmark())).isEqualTo(1);

        assertThat(bookmarkMapper.countBookmark(MEMBER_ID, POLICY_ID)).isEqualTo(1);
        assertThat(bookmarkMapper.selectBookmarkedPolicyIds(MEMBER_ID)).containsExactly(POLICY_ID);
    }

    @Test
    void 찜_삭제하면_1_반환되고_사라진다() {
        bookmarkMapper.insertBookmark(bookmark());

        assertThat(bookmarkMapper.deleteBookmark(MEMBER_ID, POLICY_ID)).isEqualTo(1);
        assertThat(bookmarkMapper.countBookmark(MEMBER_ID, POLICY_ID)).isZero();
    }

    @Test
    void 없는_찜_삭제하면_0_반환() {
        assertThat(bookmarkMapper.deleteBookmark(MEMBER_ID, POLICY_ID)).isZero();
    }

    @Test
    void 같은_찜_두번_추가하면_중복_예외() {
        bookmarkMapper.insertBookmark(bookmark());

        assertThatThrownBy(() -> bookmarkMapper.insertBookmark(bookmark()))
                .isInstanceOf(DuplicateKeyException.class);
    }
}