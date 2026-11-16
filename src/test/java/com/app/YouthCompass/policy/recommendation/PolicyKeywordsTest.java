package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicyKeywords;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 찾는 지원(키워드)이 정책 설명·지원 내용에 나오는지 (추천 관련도 +1)
class PolicyKeywordsTest {

    @Test
    void 설명이나_지원_내용에_키워드가_있으면_관련() {

        PolicyVO policy = PolicyVO.builder()
                .policyName("청년 월세 지원")
                .policySupportContent("월 최대 20만원, 목돈 마련 지원")
                .build();

        assertThat(PolicyKeywords.mentionedInText(policy, "목돈")).isTrue();
    }

    @Test
    void 목돈은_저축_자산형성으로_쓰인_정책도_관련() {

        // 청년 노동자 통장 운영: 원문에 "목돈"은 없고 "자산형성", "저축"만 있음
        PolicyVO policy = PolicyVO.builder()
                .policyName("청년 노동자 통장 운영")
                .policyDescription("도내 청년 노동자 근로 의지 고취 및 자산형성 지원을 통한 자립기반 마련")
                .policySupportContent("매월 10만원 저축, 2년 만기 시 최대 580만원 지급")
                .build();

        assertThat(PolicyKeywords.mentionedInText(policy, "목돈")).isTrue();
    }

    @Test
    void 관계없는_정책이나_빈_키워드는_관련_없음() {

        PolicyVO policy = PolicyVO.builder()
                .policyName("고립·은둔 청년 대상 선제적 sns 상담 실시")
                .policyDescription("고립·은둔 청년 발굴 및 상담 연계")
                .build();

        assertThat(PolicyKeywords.mentionedInText(policy, "목돈")).isFalse();
        assertThat(PolicyKeywords.mentionedInText(policy, null)).isFalse();
        assertThat(PolicyKeywords.mentionedInText(policy, " ")).isFalse();
    }
}
