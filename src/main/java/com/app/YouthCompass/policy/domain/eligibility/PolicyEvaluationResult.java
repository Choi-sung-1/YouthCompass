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

    // 추천 점수
    private double score;
}
