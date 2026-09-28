package com.app.YouthCompass.converter;

import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import org.springframework.stereotype.Component;

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
}