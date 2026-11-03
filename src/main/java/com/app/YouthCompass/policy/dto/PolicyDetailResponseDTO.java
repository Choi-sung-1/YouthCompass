package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 정책 상세 화면 데이터
@Getter
@Setter
public class PolicyDetailResponseDTO {

    // 정책 정보
    private PolicyVO policy;
    private PolicyListItemDTO card;          // 분야·D-day 라벨 재사용 (목록 카드와 같은 기준)
    private String ageText;                  // "만 19세 ~ 34세" / "제한 없음"
    private boolean inactive;                // API에서 사라진 정책

    // 나에게 맞는지 (로그인 회원만, 비로그인이면 evaluated = false)
    private boolean evaluated;
    private List<ConditionRow> conditions = List.of();
    private List<String> matchedReasons = List.of();   // "나이 조건(만 19~34세)에 맞아요"
    private List<String> unknownReasons = List.of();   // "학력을 알려주시면 …"
    private List<String> filledFromMember = List.of(); // 회원정보에서 가져온 항목 (나이·지역)
    private int notMatchCount;
    private int unknownCount;

    // 조건 한 줄
    public record ConditionRow(String name, EligibilityStatus status) {

        public String label() {
            return switch (status) {
                case MATCH -> "충족";
                case NOT_MATCH -> "미충족";
                case UNKNOWN -> "확인 필요";
            };
        }

        // common.css 의 뱃지 클래스
        public String badgeClass() {
            return switch (status) {
                case MATCH -> "badge-ok";
                case NOT_MATCH -> "badge-no";
                case UNKNOWN -> "badge-unknown";
            };
        }
    }

    // 종합 결론 문장
    public String verdict() {
        if (notMatchCount > 0) {
            return "맞지 않는 조건이 " + notMatchCount + "개 있어요. 신청 대상이 아닐 수 있어요.";
        }
        if (unknownCount > 0) {
            return "확인이 필요한 조건이 " + unknownCount + "개 있어요. 아래 내용을 공고에서 확인해 주세요.";
        }
        return "확인된 조건을 모두 충족해요. 신청해 볼 만해요!";
    }

    public String verdictClass() {
        if (notMatchCount > 0) return "badge-no";
        if (unknownCount > 0) return "badge-unknown";
        return "badge-ok";
    }
}