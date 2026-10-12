package com.app.YouthCompass.policy.domain.eligibility;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import lombok.Data;

@Data
public class PolicyEvaluationResult {

    // 평가한 정책
    private PolicyVO policy;

    // 조건별 판정 결과
    private EligibilityStatus ageResult;
    private EligibilityStatus regionResult;
    private EligibilityStatus schoolResult;
    private EligibilityStatus jobResult;
    private EligibilityStatus marriageResult;
    private EligibilityStatus incomeResult;
    private EligibilityStatus majorResult;
    private EligibilityStatus specialTargetResult;

    // 판정 개수
    private int matchCount;
    private int notMatchCount;
    private int unknownCount;

//    → 이 사용자의 구체적인 자격조건과 얼마나 잘 맞는가에 대한 점수
    private int specificMatchScore;

//    → 사용자가 찾는 분야와 얼마나 관련 있는가에 대한 점수
    private int relevanceScore;
}
