package com.app.YouthCompass.policy.external;

import com.app.YouthCompass.policy.external.dto.YouthPolicyDTO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// YouthPolicyDTO --> PolicyVO 변환
@Component
public class PolicyConverter {

    public PolicyVO convert(YouthPolicyDTO dto) {

        return PolicyVO.builder()

                // ================================
                // 기본 정책 정보
                // ================================

                .policySource("YOUTH_CENTER")
                .policyExternalId(dto.getPlcyNo())

                .policyName(dto.getPlcyNm())
                .policyDescription(dto.getPlcyExplnCn())
                .policyKeywords(dto.getPlcyKywdNm())

                .policyLargeCategory(dto.getLclsfNm())
                .policyMediumCategory(dto.getMclsfNm())

                .policySupportContent(dto.getPlcySprtCn())

                .policyOrganizationName(dto.getSprvsnInstCdNm())

                .policyApplicationMethod(dto.getPlcyAplyMthdCn())
                .policyApplicationUrl(dto.getAplyUrlAddr())

                .policySubmissionDocuments(dto.getSbmsnDcmntCn())
                .policyReferenceUrl(dto.getRefUrlAddr1())


                // ================================
                // 자격 조건
                // ================================

                // 나이
                .policyMinAge(parseInteger(dto.getSprtTrgtMinAge()))
                .policyMaxAge(parseInteger(dto.getSprtTrgtMaxAge()))

                // 학력
                .policySchoolCodes(dto.getSchoolCd())

                // 취업 상태
                .policyJobCodes(dto.getJobCd())

                // 혼인 상태
                .policyMarriageStatusCodes(dto.getMrgSttsCd())

                // 소득 조건
                .policyIncomeConditionCode(dto.getEarnCndSeCd())
                .policyIncomeMinAmount(parseLong(dto.getEarnMinAmt()))
                .policyIncomeMaxAmount(parseLong(dto.getEarnMaxAmt()))
                .policyIncomeEtcCondition(dto.getEarnEtcCn())

                // 전공
                .policyMajorCodes(dto.getPlcyMajorCd())

                // 특화 대상
                .policySpecialTargetCodes(dto.getSbizCd())

                // 지역
                .policyRegionCodes(dto.getZipCd())

                // 기타 추가 자격조건
                .policyAdditionalCondition(dto.getAddAplyQlfcCndCn())

                // ================================
                // 신청기간
                // ================================

                .policyApplyPeriodCode(dto.getAplyPrdSeCd())
                .policyApplyStartDate(parseApplyStartDate(dto.getAplyYmd()))
                .policyApplyEndDate(parseApplyEndDate(dto.getAplyYmd()))


                // ================================
                // 관리 정보
                // ================================

                .policyStatus("ACTIVE")

                .build();
    }


    // String -> Integer
    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }


    // String -> Long
    private Long parseLong(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }
    // 날짜 모양: 20261030, 2026-10-30, 2026.10.30 모두 인식
    private static final Pattern DATE_PATTERN =
            Pattern.compile("(\\d{4})[-.]?(\\d{2})[-.]?(\\d{2})");

    // "20261030 ~ 20261031" → [2026-10-30, 2026-10-31]
    // 날짜가 없으면(상시 모집 등) 빈 리스트
    private List<LocalDate> parseApplyDates(String aplyYmd) {

        List<LocalDate> dates = new ArrayList<>();

        if (aplyYmd == null || aplyYmd.isBlank()) {
            return dates;
        }

        Matcher matcher = DATE_PATTERN.matcher(aplyYmd);
        while (matcher.find()) {
            try {
                dates.add(LocalDate.of(
                        Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3))));
            } catch (DateTimeException e) {
                // 20261399 같은 잘못된 날짜는 건너뜀
            }
        }
        return dates;
    }

    // 신청 시작일 = 첫 번째 날짜
    private LocalDate parseApplyStartDate(String aplyYmd) {
        List<LocalDate> dates = parseApplyDates(aplyYmd);
        return dates.isEmpty() ? null : dates.get(0);
    }

    // 신청 마감일 = 마지막 날짜
    private LocalDate parseApplyEndDate(String aplyYmd) {
        List<LocalDate> dates = parseApplyDates(aplyYmd);
        return dates.isEmpty() ? null : dates.get(dates.size() - 1);
    }
}