package com.app.YouthCompass.history.domain;

import lombok.Data;

import java.time.LocalDateTime;

// 추천 기록 (TBL_RECOMMEND_HISTORY) — 추천 1회 = 1행
@Data
public class RecommendHistoryVO {
    //    추천 기록 PK (INSERT 후 MyBatis가 생성된 값을 채워 줌)
    private Long historyId;
    //    회원 PK (TBL_MEMBER.MEMBER_ID)
    private Long memberId;
    //    사용자가 입력한 문장
    private String sentence;
    //    AI가 추출한 조건 (화면 표시용 JSON 문자열)
    private String profileJson;
    //    추천 시각 (DB 기본값 CURRENT_TIMESTAMP)
    private LocalDateTime createdAt;
}