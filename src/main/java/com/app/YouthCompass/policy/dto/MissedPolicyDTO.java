package com.app.YouthCompass.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 조건 하나 차이로 놓친 정책 (값은 A의 MissedPolicyFinder가 채움)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissedPolicyDTO {

    private Long policyId;
    private String policyName;
    private String missedCondition;  // 걸린 조건 이름 ex) "소득"
    private String hint;             // 안내 ex) "연소득 3,600만 원 이하면 신청할 수 있어요"
}