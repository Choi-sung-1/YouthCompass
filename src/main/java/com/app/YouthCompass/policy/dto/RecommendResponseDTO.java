package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 추천 응답: 화면 한 장에 필요한 전체 정보
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResponseDTO {

    // AI가 이해한 사용자 조건 (나이, 지역, 학력 ...)
    private UserPolicyProfileVO profile;

    // AI가 이해한 검색 목적 (분야, 키워드)
    private PolicySearchIntent intent;

    // 추천 정책 목록 (최대 10개)
    private List<RecommendedPolicyDTO> policies;

    // 문장에 없어서 로그인 회원정보로 채운 항목 ex) ["나이", "지역"]
    private List<String> memberFilled;

    // AI가 개인 맞춤으로 다시 고른 결과인지 (false면 규칙 기반 순서 + 기본 문장)
    private boolean personalized;
}
