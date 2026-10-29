package com.app.YouthCompass.policy.domain.recommendation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

// 정책 대분류 (온통청년 DB의 POLICY_LARGE_CATEGORY 값과 같은 한글 이름)
// AI 프롬프트, 조건 수정 화면 선택지, AI 응답 정리에서 같이 사용
public final class PolicyCategories {

    public static final List<String> ALL =
            List.of("일자리", "주거", "교육", "복지문화", "참여권리");

    // AI가 영문·비슷한 말로 돌려줬을 때 한글 대분류로 바꾸는 표
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("JOB", "일자리"),
            Map.entry("JOBS", "일자리"),
            Map.entry("EMPLOYMENT", "일자리"),
            Map.entry("WORK", "일자리"),
            Map.entry("취업", "일자리"),
            Map.entry("창업", "일자리"),
            Map.entry("HOUSING", "주거"),
            Map.entry("RESIDENCE", "주거"),
            Map.entry("주택", "주거"),
            Map.entry("EDUCATION", "교육"),
            Map.entry("학업", "교육"),
            Map.entry("WELFARE", "복지문화"),
            Map.entry("CULTURE", "복지문화"),
            Map.entry("WELFARE_CULTURE", "복지문화"),
            Map.entry("복지", "복지문화"),
            Map.entry("문화", "복지문화"),
            Map.entry("PARTICIPATION", "참여권리"),
            Map.entry("RIGHTS", "참여권리"),
            Map.entry("PARTICIPATION_RIGHTS", "참여권리"),
            Map.entry("참여", "참여권리"),
            Map.entry("권리", "참여권리")
    );

    private PolicyCategories() {
    }

    // 한글 대분류로 정리. 알 수 없는 값이면 null (관련도 계산에서 분야 없음으로 처리)
    public static String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        if (ALL.contains(trimmed)) {
            return trimmed;
        }

        String key = trimmed.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return ALIASES.get(key);
    }
}
