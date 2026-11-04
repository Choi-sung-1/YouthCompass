package com.app.YouthCompass.bookmark;

import com.app.YouthCompass.bookmark.domain.BookmarkVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    //    찜 토글 (반환: true = 이제 찜 상태, false = 이제 찜 취소 상태)
    //    먼저 삭제를 시도 → 지워졌으면 원래 찜이 있었던 것 → 취소로 끝
    //                     → 지운 게 없으면 원래 찜이 없었던 것 → 추가
    @Transactional
    public boolean toggleBookmark(Long memberId, Long policyId) {
        if (memberId == null || policyId == null) {
            throw new IllegalArgumentException("찜 토글에는 회원 ID와 정책 ID가 필요합니다.");
        }

        if (bookmarkMapper.deleteBookmark(memberId, policyId) > 0) {
            log.debug("찜 취소 memberId={}, policyId={}", memberId, policyId);
            return false;
        }

        BookmarkVO bookmark = new BookmarkVO();
        bookmark.setMemberId(memberId);
        bookmark.setPolicyId(policyId);

        try {
            bookmarkMapper.insertBookmark(bookmark);
        } catch (DuplicateKeyException e) {
            // 버튼 연타로 다른 요청이 먼저 추가한 경우 → 이미 찜 상태이므로 그대로 성공 처리
            log.debug("찜 중복 요청 무시 memberId={}, policyId={}", memberId, policyId);
        }
        log.debug("찜 추가 memberId={}, policyId={}", memberId, policyId);
        return true;
    }

    //    [5장 약속] 찜 여부 (비로그인이면 false)
    public boolean isBookmarked(Long memberId, Long policyId) {
        if (memberId == null || policyId == null) {
            return false;
        }
        return bookmarkMapper.countBookmark(memberId, policyId) > 0;
    }

    //    [5장 약속] 회원이 찜한 정책 ID 전체 (목록 화면에서 한 번에 조회용, 비로그인이면 빈 Set)
    public Set<Long> findBookmarkedPolicyIds(Long memberId) {
        if (memberId == null) {
            return new HashSet<>();
        }
        return new HashSet<>(bookmarkMapper.selectBookmarkedPolicyIds(memberId));
    }
}