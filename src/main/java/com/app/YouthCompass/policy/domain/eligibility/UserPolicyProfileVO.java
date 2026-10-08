package com.app.YouthCompass.policy.domain.eligibility;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder

// 사용자 현 상태 조건 코드
public class UserPolicyProfileVO {

    // 사용자 나이
    private Integer age;

    // 거주 지역
    private Region region;

    // 학력 상태
    private SchoolRequirement school;

    // 취업 상태
    private JobRequirement job;

    // 혼인 상태
    private MarriageRequirement marriage;

    // 연소득
    private Long annualIncome;

    // 전공
    private MajorRequirement major;

    // 특화 대상
    private Set<SpecialTargetRequirement> specialTargets;
}