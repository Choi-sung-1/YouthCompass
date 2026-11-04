package com.app.YouthCompass.history.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 추천 기록 상세 화면 한 장에 필요한 전체 정보
@Getter
@AllArgsConstructor
public class RecommendHistoryDetailDTO {

    //    추천 기록 PK
    private Long historyId;
    //    그때 입력한 문장
    private String sentence;
    //    추천받은 시각
    private LocalDateTime createdAt;
    //    그때 AI가 이해한 조건 (PROFILE_JSON → {"나이":"25세", "지역":"경기도", ...}, 저장 순서 유지)
    private Map<String, String> conditions;
    //    그때의 TOP 10 (순위 순)
    private List<RecommendHistoryItemDTO> items;
}