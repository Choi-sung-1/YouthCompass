package com.app.YouthCompass.bookmark.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookmarkVO {
    //    회원 PK (TBL_MEMBER.MEMBER_ID)
    private Long memberId;
    //    정책 PK (TBL_POLICY.POLICY_ID)
    private Long policyId;
    //    찜한 시각 (DB 기본값 CURRENT_TIMESTAMP)
    private LocalDateTime createdAt;
}
