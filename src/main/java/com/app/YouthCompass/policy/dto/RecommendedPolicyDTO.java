package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 추천 결과 카드 한 장에 들어갈 정보
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPolicyDTO {

    // 정책 PK
    private Long policyId;

    // 정책명
    private String policyName;

    // 주관 기관명
    private String organizationName;

    // 지원 내용
    private String supportContent;

    // 신청 링크 (신청 URL이 없으면 참고 URL)
    private String applicationUrl;

    // 검색 내용(분야·키워드)과 관련된 정도
    private int relevanceScore;

    // 사용자 자격조건과 구체적으로 맞는 정도
    private int matchScore;

    // 추천 이유 ex) "나이 조건 충족" → 10번에서 채움
    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    // 확인이 필요한 조건 ex) "소득 조건은 공고 확인 필요" → 10번에서 채움
    @Builder.Default
    private List<String> unknownReasons = new ArrayList<>();

    // 판정 결과(PolicyEvaluationResult)를 카드용 DTO로 변환
    public static RecommendedPolicyDTO from(PolicyEvaluationResult result) {

        PolicyVO policy = result.getPolicy();

        return RecommendedPolicyDTO.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyName())
                .organizationName(policy.getPolicyOrganizationName())
                .supportContent(policy.getPolicySupportContent())
                .applicationUrl(chooseUrl(policy))
                .relevanceScore(result.getRelevanceScore())
                .matchScore(result.getSpecificMatchScore())
                .build();
    }

    private static String chooseUrl(PolicyVO policy) {

        String url = policy.getPolicyApplicationUrl();

        if (url != null && !url.isBlank()) {
            return url;
        }

        return policy.getPolicyReferenceUrl();
    }
}
