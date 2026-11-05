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

    // 참여 제한 대상 (ptcpPrpTrgtCn, 판정에는 안 쓰고 화면 안내용)
    private String policyParticipationRestriction;

    // ================================
    // 신청 방식 정보
    // ================================

    // 심사 방법 (srngMthdCn)
    private String policyScreeningMethod;

    // 지원 규모 원문 (sprtSclCnt, "0"·빈 값은 null)
    private String policySupportScale;

    // 지원 도착순서 여부 원본 (sprtArvlSeqYn)
    // 화면에 "선착순" 배지로 쓰지 않음: Y인 정책 256개 중 원문에 선착순이 나오는 건 30개뿐이라 선착순 표시로 믿기 어려움 (2026-11 확인)
    private String policyArrivalOrderYn;

    // 지원 규모 제한 여부 원본 (sprtSclLmtYn, 의미 확인 후 사용)
    private String policyScaleLimitYn;

    // AI가 정리한 제출 서류 목록 (줄바꿈 구분 / null = 정리 전·실패 / 빈 값 = 서류 없음)
    private String policyDocumentItems;

    // 정리할 때 쓴 제출서류 원문의 해시 (원문이 바뀌면 다시 정리)
    private String policyDocumentSourceHash;
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
    // 사업기간 / 기타 (화면 안내용)
    // ================================

    // 사업기간 시작일·종료일 (bizPrdBgngYmd / bizPrdEndYmd)
    private LocalDate policyBusinessStartDate;
    private LocalDate policyBusinessEndDate;

    // 사업기간 기타 (bizPrdEtcCn, 날짜 대신 "연중 계속" 등)
    private String policyBusinessPeriodEtc;

    // 기타 사항 (etcMttrCn)
    private String policyEtcNote;

    // ================================
    // 정책 관리 정보
    // ================================

    // 정책 상태
    private String policyStatus;

    // 정책 DB 등록일
    private LocalDateTime policyCreatedAt;

    // 정책 DB 수정일
    private LocalDateTime policyUpdatedAt;

    // ================================
    // 링크 정리
    // ================================
    // API 원본에 "www.xxx.or.kr"(http 없음), 앞뒤 공백(전각 포함), "-", "전화문의" 같은 값이 섞여 있음.
    // http(s)가 없으면 브라우저가 우리 사이트 내부 경로로 해석해 "신청하러 가기"가 같은 화면으로 열리므로
    // 화면·캘린더·저장 모두 이 getter를 거치게 해서 절대 URL 또는 null 만 나가게 한다.

    public String getPolicyApplicationUrl() {
        return normalizeUrl(policyApplicationUrl);
    }

    public String getPolicyReferenceUrl() {
        return normalizeUrl(policyReferenceUrl);
    }

    static String normalizeUrl(String raw) {
        if (raw == null) {
            return null;
        }
        // 일반 공백 + 전각 공백(U+3000) + NBSP 제거
        String url = raw.replaceAll("^[\\s\\u3000\\u00A0]+|[\\s\\u3000\\u00A0]+$", "");
        if (url.isEmpty()) {
            return null;
        }
        String lower = url.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return url;
        }
        if (lower.startsWith("//")) {
            return "https:" + url;
        }
        // 공백 없는 "도메인.tld[/경로]" 모양이면 https 붙임, 아니면("-", "전화문의") 링크로 쓰지 않음
        if (url.matches("^[A-Za-z0-9][A-Za-z0-9-]*(\\.[A-Za-z0-9-]+)+(:\\d+)?([/?#]\\S*)?$")) {
            return "https://" + url;
        }
        return null;
    }
}