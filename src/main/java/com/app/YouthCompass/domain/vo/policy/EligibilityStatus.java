package com.app.YouthCompass.domain.vo.policy;

// 조건 충족 결과값
public enum EligibilityStatus {
    // 조건 충족
    MATCH,
    // 조건 불충족
    NOT_MATCH,
    // 정보 부족으로 판정 불가
    UNKNOWN
}