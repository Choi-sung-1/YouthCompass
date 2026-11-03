package com.app.YouthCompass.policy.dto;

import java.util.List;

// 홈 화면 위젯 데이터
public record HomeWidgetDTO(
        int totalCount,                          // 전체 정책 (ACTIVE)
        int openCount,                           // 지금 신청 가능한 정책
        List<PolicyListItemDTO> closingSoon,     // 마감 임박
        List<PolicyListItemDTO> recentlyOpened   // 최근 모집 시작
) {
}