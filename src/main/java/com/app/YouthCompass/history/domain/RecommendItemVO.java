package com.app.YouthCompass.history.domain;

import lombok.Data;

// 추천 기록 안의 정책 (TBL_RECOMMEND_ITEM) — 기록 1건당 TOP 10
@Data
public class RecommendItemVO {
    //    추천 항목 PK
    private Long itemId;
    //    어느 추천 기록에 속하는지 (TBL_RECOMMEND_HISTORY.HISTORY_ID)
    private Long historyId;
    //    순위 (1부터)
    private Integer rankNo;
    //    정책 PK (TBL_POLICY.POLICY_ID)
    private Long policyId;
    //    추천 이유 (AI 문장 그대로 저장 → 다시 볼 때 AI 재호출 없음)
    private String reason;
    //    기대 효과 (AI 문장 그대로 저장)
    private String effect;
}