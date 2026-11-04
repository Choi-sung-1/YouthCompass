package com.app.YouthCompass.policy.dto;

import lombok.Data;

// POST /policy/recheck 요청 (JSON)
@Data
public class RecheckRequestDTO {

    // 다시 판정할 정책
    private Long policyId;

    // 조건 수정 폼의 지금 값 + 이 카드에서 답한 값 (형식은 조건 수정 폼과 같음, 빈 값 = 모름)
    private ConditionRequestDTO conditions;

    // 지원 대상 질문에서 "해당 없음"을 골랐는지 (null = 모름과 구분하기 위해)
    private boolean noSpecialTarget;
}