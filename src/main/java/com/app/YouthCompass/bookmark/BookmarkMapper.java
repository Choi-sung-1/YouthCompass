package com.app.YouthCompass.bookmark;

import com.app.YouthCompass.bookmark.domain.BookmarkVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    // 찜 추가 (이미 있으면 PK 중복 예외 → Service에서 처리)
    int insertBookmark(BookmarkVO bookmark);

    // 찜 삭제 (반환: 삭제된 행 수, 0이면 원래 찜이 없었음)
    int deleteBookmark(@Param("memberId") Long memberId, @Param("policyId") Long policyId);

    // 찜 여부 확인 (반환: 0 또는 1)
    int countBookmark(@Param("memberId") Long memberId, @Param("policyId") Long policyId);

    // 회원이 찜한 정책 ID 전체 (목록 화면에서 한 번에 조회용)
    List<Long> selectBookmarkedPolicyIds(Long memberId);

    // 찜 목록 화면용: 찜한 정책 정보 (최근에 찜한 순, TBL_POLICY에 없는 정책은 제외)
    List<PolicyVO> selectBookmarkedPolicies(Long memberId);

}