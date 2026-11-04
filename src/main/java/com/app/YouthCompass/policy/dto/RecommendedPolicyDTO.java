package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    // 추천 이유: 왜 이 사용자에게 맞는지 (AI가 사용자 상황과 연결해 생성, 실패 시 규칙 문장)
    private String recommendReason;

    // 기대 효과: 받으면 이 사용자에게 무엇이 좋아지는지 (지원내용 숫자만 사용, 실패 시 지원내용 앞부분)
    private String expectedEffect;

    // AI 맞춤 문장인지 (화면 배지 표시용)
    private boolean personalized;

    // 충족한 자격 조건 ex) "나이 조건(만 19~34세)에 맞아요"
    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    // 확인이 필요한 조건 ex) "소득 조건은 공고에서 확인이 필요해요"
    @Builder.Default
    private List<String> unknownReasons = new ArrayList<>();

    // 마감 표시는 정책 목록·찜 목록·추천 기록과 같은 계산(PolicyListItemDTO.from())을 재사용
    private LocalDate applyEndDate;      // 신청 마감일 (상시면 null)
    private String deadlineLabel;        // "D-3", "오늘 마감", "상시", "마감", "기간 확인 필요"
    private boolean closingSoon;         // 마감 7일 이내
    private String applicationMethod;    // 신청 방법 원문

    // 제출 서류 체크리스트 (B-2에서 채움)
    @Builder.Default
    private List<String> documents = new ArrayList<>();

    private String screeningMethod;      // 심사 방법 (A의 ② 머지 전엔 null)
    private String supportScale;         // 모집 규모 숫자만 ex) "300" (단위가 정책마다 달라 붙이지 않음)
    // 판정 결과(PolicyEvaluationResult)를 카드용 DTO로 변환
    public static RecommendedPolicyDTO from(PolicyEvaluationResult result) {

        PolicyVO policy = result.getPolicy();

        // 마감 표시는 정책 목록·찜 목록·추천 기록과 같은 계산을 재사용 (A의 PolicyListItemDTO — 호출만)
        PolicyListItemDTO card = PolicyListItemDTO.from(policy);

        return RecommendedPolicyDTO.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyName())
                .organizationName(policy.getPolicyOrganizationName())
                .supportContent(policy.getPolicySupportContent())
                .applicationUrl(chooseUrl(policy))
                .relevanceScore(result.getRelevanceScore())
                .matchScore(result.getSpecificMatchScore())
                .applyEndDate(policy.getPolicyApplyEndDate())
                .deadlineLabel(card.getDeadlineLabel())
                .closingSoon(card.isClosingSoon())
                .applicationMethod(policy.getPolicyApplicationMethod())
                .documents(splitDocuments(policy.getPolicySubmissionDocuments()))
                .screeningMethod(policy.getPolicyScreeningMethod())
                .supportScale(policy.getPolicySupportScale())
                .build();
    }
    private static String chooseUrl(PolicyVO policy) {

        String url = policy.getPolicyApplicationUrl();

        if (url != null && !url.isBlank()) {
            return url;
        }

        return policy.getPolicyReferenceUrl();
    }

    // 체크리스트에 보여줄 최대 서류 수
    private static final int DOCUMENT_LIMIT = 15;

    // 제출서류 원문 → 항목 목록 (괄호 안 구분자는 무시, 중복·빈 항목 제거)
    private static List<String> splitDocuments(String text) {

        List<String> items = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return items;
        }

        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (char c : text.toCharArray()) {
            if (c == '(' || c == '[') {
                depth++;
            } else if ((c == ')' || c == ']') && depth > 0) {
                depth--;
            }

            boolean separator = c == '\n'
                    || (depth == 0 && (c == ',' || c == '·' || c == 'ㆍ' || c == ';'));

            if (separator) {
                addDocument(items, current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        addDocument(items, current.toString());

        return items.size() > DOCUMENT_LIMIT
                ? new ArrayList<>(items.subList(0, DOCUMENT_LIMIT))
                : items;
    }

    // 앞의 기호·번호(-, •, ○, ※, 1. 2) 등) 제거 후 추가
    private static void addDocument(List<String> items, String raw) {

        String item = raw
                .replaceAll("^[\\s\\-*•○●□■▶※]+", "")
                .replaceAll("^\\d+[.)]\\s*", "")
                .trim();

        if (!item.isEmpty() && !items.contains(item)) {
            items.add(item);
        }
    }
}
