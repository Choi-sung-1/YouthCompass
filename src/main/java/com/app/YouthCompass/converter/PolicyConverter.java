package com.app.YouthCompass.converter;

import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import org.springframework.stereotype.Component;

@Component
public class PolicyConverter {

    public PolicyVO convert(YouthPolicyDTO dto) {

        return PolicyVO.builder()

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

                .policyMinAge(parseInteger(dto.getSprtTrgtMinAge()))

                .policyMaxAge(parseInteger(dto.getSprtTrgtMaxAge()))

                .policyAdditionalCondition(dto.getAddAplyQlfcCndCn())

                .policyRegionCodes(dto.getZipCd())

                .policyStatus("ACTIVE")

                .build();
    }
// String-> Integer 캐스팅
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
}