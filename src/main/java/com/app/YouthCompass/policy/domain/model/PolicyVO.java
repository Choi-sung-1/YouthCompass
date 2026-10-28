package com.app.YouthCompass.policy.domain.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyVO {

    // 정책 PK
    private Long policyId;

    // 정책 데이터 출처
    private String policySource;

    // 외부 정책 고유 번호
    private String policyExternalId;

    // 정책명
    private String policyName;

    // 정책 설명
    private String policyDescription;

    // 정책 키워드
    private String policyKeywords;

    // 정책 대분류
    private String policyLargeCategory;

    // 정책 중분류
    private String policyMediumCategory;

    // 정책 지원 내용
    private String policySupportContent;

    // 주관 기관명
    private String policyOrganizationName;

    // 정책 신청 방법
    private String policyApplicationMethod;

    // 정책 신청 URL
    private String policyApplicationUrl;

    // 제출 서류
    private String policySubmissionDocuments;

    // 참고 URL
    private String policyReferenceUrl;


    // ================================
    // 자격 조건
    // ================================

    // 지원 최소 나이
    private Integer policyMinAge;

    // 지원 최대 나이
    private Integer policyMaxAge;

    // 학력 조건 코드
    private String policySchoolCodes;

    // 취업 상태 코드
    private String policyJobCodes;

    // 혼인 상태 코드
    private String policyMarriageStatusCodes;

    // 소득 조건 구분 코드
    private String policyIncomeConditionCode;

    // 최소 소득
    private Long policyIncomeMinAmount;

    // 최대 소득
    private Long policyIncomeMaxAmount;

    // 소득 기타 조건
    private String policyIncomeEtcCondition;

    // 전공 코드
    private String policyMajorCodes;

    // 특화 대상 코드
    private String policySpecialTargetCodes;

    // 지원 지역 코드 목록
    private String policyRegionCodes;

    // 추가 신청 자격조건
    private String policyAdditionalCondition;

    // ================================
    // 신청기간
    // ================================

    // 신청기간 구분 코드 (0057001 특정기간 / 0057002 상시 / 0057003 마감)
    private String policyApplyPeriodCode;

    // 신청 시작일 (aplyYmd 앞쪽, 상시면 null)
    private LocalDate policyApplyStartDate;

    // 신청 마감일 (aplyYmd 뒤쪽, 상시면 null) → 오늘보다 이전이면 추천 제외
    private LocalDate policyApplyEndDate;

    // ================================
    // 정책 관리 정보
    // ================================

    // 정책 상태
    private String policyStatus;

    // 정책 DB 등록일
    private LocalDateTime policyCreatedAt;

    // 정책 DB 수정일
    private LocalDateTime policyUpdatedAt;
}