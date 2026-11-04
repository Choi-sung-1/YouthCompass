package com.app.YouthCompass.profile.domain;

import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 회원별 조건 프로필 (TBL_MEMBER_PROFILE, 회원 1:1)
// enum은 MyBatis가 이름(UNIVERSITY_STUDENT 등)으로 저장·복원함
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileVO {

    // 회원 PK
    private Long memberId;

    // 시·군 단위코드 (SigunguRegistry, 예: 평택 "41220")
    private String sigunguCode;

    // 학력
    private SchoolRequirement school;

    // 취업 상태
    private JobRequirement job;

    // 혼인 상태
    private MarriageRequirement marriage;

    // 전공
    private MajorRequirement major;

    // 연소득 (만원)
    private Long annualIncome;

    // 수정 시각
    private LocalDateTime updatedAt;
}