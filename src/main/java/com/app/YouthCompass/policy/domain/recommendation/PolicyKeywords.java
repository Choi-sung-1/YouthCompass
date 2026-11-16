package com.app.YouthCompass.policy.domain.recommendation;

import com.app.YouthCompass.policy.domain.model.PolicyVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// 찾는 지원(키워드)이 정책 본문에 나오는지
// 추천 관련도(PolicyRecommendationService), 추천 이유(RecommendReasonBuilder), 놓친 정책(MissedPolicyFinder)에서 같이 사용
public final class PolicyKeywords {

    // 사용자가 쓰는 말과 정책 원문이 쓰는 말이 다른 경우
    // ex) "목돈 모으는 정책" ↔ 청년 노동자 통장 "자산형성 지원 … 매월 10만원 저축"
    // 키워드는 문장에 나온 단어로만 뽑아서(openAiService) 원문 표현을 AI가 대신 골라줄 수 없음 → 여기서 넓힘
    private static final Map<String, List<String>> RELATED = Map.of(
            "목돈", List.of("저축", "적금", "자산형성", "자산 형성"),
            "저축", List.of("목돈", "적금", "자산형성", "자산 형성"),
            "적금", List.of("목돈", "저축", "자산형성", "자산 형성"),
            "자산", List.of("목돈", "저축", "적금")
    );

    private PolicyKeywords() {}

    // 정책 이름·설명·지원 내용에 키워드(또는 비슷한 말)가 있는지
    // 이름·키워드 칸에 없는 지원 내용 단어는 설명에만 있는 경우가 많음
    public static boolean mentionedInText(PolicyVO policy, String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return false;
        }

        return Stream.concat(Stream.of(keyword.trim()), RELATED.getOrDefault(keyword.trim(), List.of()).stream())
                .anyMatch(word -> contains(policy.getPolicyName(), word)
                        || contains(policy.getPolicyDescription(), word)
                        || contains(policy.getPolicySupportContent(), word));
    }

    private static boolean contains(String text, String word) {
        return text != null && text.contains(word);
    }
}
