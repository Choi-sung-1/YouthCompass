package com.app.YouthCompass.policy.domain.eligibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 사용자 현 상태 조건 코드
// 이걸로 정책 추천
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

    // 개인 연소득 (단위: 만원) - 정책 earnMin/MaxAmt와 동일 단위
     private Long annualIncome;

    // 전공
    private MajorRequirement major;

    // 특화 대상
    private Set<SpecialTargetRequirement> specialTargets;
}