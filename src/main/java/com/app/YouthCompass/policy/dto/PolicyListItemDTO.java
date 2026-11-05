package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.model.PolicyText;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

// 정책 목록 카드 하나
@Getter
public class PolicyListItemDTO {

    private static final int SUMMARY_LIMIT = 90;
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private Long policyId;
    private String policyName;
    private String organizationName;
    private String largeCategory;
    private String summary;            // 정책 설명 앞부분
    private LocalDate applyEndDate;
    private String deadlineLabel;      // "D-3", "오늘 마감", "상시", "마감", "기간 확인 필요"
    private boolean closingSoon;       // 마감 7일 이내 (강조 표시용)
    private boolean closed;

    public static PolicyListItemDTO from(PolicyVO p) {

        PolicyListItemDTO dto = new PolicyListItemDTO();
        dto.policyId = p.getPolicyId();
        dto.policyName = p.getPolicyName();
        dto.organizationName = PolicyText.text(p.getPolicyOrganizationName());
        dto.largeCategory = PolicyText.text(p.getPolicyLargeCategory());
        dto.summary = cut(p.getPolicyDescription(), SUMMARY_LIMIT);
        dto.applyEndDate = p.getPolicyApplyEndDate();
        dto.fillDeadline(p.getPolicyApplyPeriodCode());
        return dto;
    }

    // 신청기간 구분: 0057001 특정기간 / 0057002 상시 / 0057003 마감
    private void fillDeadline(String periodCode) {

        if ("0057003".equals(periodCode)) {
            closed = true;
            deadlineLabel = "마감";
            return;
        }
        if ("0057002".equals(periodCode)) {
            deadlineLabel = "상시";
            return;
        }
        if (applyEndDate == null) {
            deadlineLabel = "기간 확인 필요";
            return;
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(KOREA), applyEndDate);

        if (days < 0) {
            closed = true;
            deadlineLabel = "마감";
        } else if (days == 0) {
            closingSoon = true;
            deadlineLabel = "오늘 마감";
        } else {
            closingSoon = days <= 7;
            deadlineLabel = "D-" + days;
        }
    }

    private static String cut(String text, int limit) {
        if (text == null) {
            return "";
        }
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= limit ? oneLine : oneLine.substring(0, limit) + "…";
    }
}