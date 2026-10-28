package com.app.YouthCompass.policy.dto;

import lombok.Data;

// 추천 요청: 사용자가 입력창에 쓴 문장
// ex) "수원 사는 25살 대학생이고 월세 지원을 찾고 있어요"
@Data
public class RecommendRequestDTO {

    private String sentence;
}
