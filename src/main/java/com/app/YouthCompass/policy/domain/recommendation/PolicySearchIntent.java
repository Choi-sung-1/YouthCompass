package com.app.YouthCompass.policy.domain.recommendation;

import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class PolicySearchIntent {

    // 사용자가 원하는 큰 분야
    // ex) 주거
    private String category;

    // 사용자의 구체적인 검색 목적
    // ex) 월세
    private String keyword;
}