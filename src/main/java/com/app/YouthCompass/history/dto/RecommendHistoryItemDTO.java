package com.app.YouthCompass.history.dto;

import lombok.Data;

import java.time.LocalDate;

// 추천 기록 상세의 카드 한 장 (TBL_RECOMMEND_ITEM + TBL_POLICY LEFT JOIN)
@Data
public class RecommendHistoryItemDTO {

    // ---------- DB에서 채움 (매퍼) ----------
//    순위 (1부터)
    private Integer rankNo;
    //    정책 PK
    private Long policyId;
    //    저장해 둔 추천 이유·기대 효과 (AI 재호출 없음)
    private String reason;
    private String effect;
    //    정책 정보 (TBL_POLICY에서 정책을 못 찾으면 전부 null)
    private String policyName;
    private String policyOrganizationName;
    private String policyLargeCategory;
    private String policyApplyPeriodCode;
    private LocalDate policyApplyEndDate;

    // ---------- Service에서 채움 (지금 기준 마감 표시, PolicyListItemDTO 계산 재사용) ----------
//    "D-3", "오늘 마감", "상시", "마감", "기간 확인 필요", "정책 정보 없음"
    private String deadlineLabel;
    //    마감 7일 이내 (강조 표시용)
    private boolean closingSoon;
    //    마감됨
    private boolean closed;
}