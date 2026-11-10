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

    // 문장에 분야를 직접 말했다고 볼 단어 (분야 이름과 그 앞뒤 절반)
    // 취업·창업처럼 신분(취준생 등)으로도 쓰이는 말은 넣지 않음 → 그런 말은 키워드로 들어올 때만 인정
    private static final Map<String, List<String>> MENTIONS = Map.of(
            "일자리", List.of("일자리"),
            "주거", List.of("주거"),
            "교육", List.of("교육"),
            "복지문화", List.of("복지", "문화"),
            "참여권리", List.of("참여", "권리")
    );

    // 대분류 → DB에 실제로 들어 있는 이름들
    // 온통청년 API가 분류 이름을 바꿔서 예전 이름과 새 이름이 섞여 있음 (복지문화 / 금융･복지･문화, 참여권리 / 참여･기반)
    // 교육 / 교육･직업훈련은 "교육"을 포함하므로 하나로 충분
    private static final Map<String, List<String>> DB_NAMES = Map.of(
            "일자리", List.of("일자리"),
            "주거", List.of("주거"),
            "교육", List.of("교육"),
            "복지문화", List.of("복지문화", "금융･복지･문화"),
            "참여권리", List.of("참여권리", "참여･기반")
    );

    private PolicyCategories() {
    }

    // 정책의 대분류(여러 개가 쉼표로 들어 있을 수 있음)가 이 분야인지
    public static boolean matches(String category, String policyLargeCategory) {

        if (category == null || policyLargeCategory == null) {
            return false;
        }
        return dbNames(category).stream().anyMatch(policyLargeCategory::contains);
    }

    // 이 분야로 볼 DB 대분류 이름들 (목록 검색 SQL에서 LIKE로 사용). 모르는 값은 그대로
    public static List<String> dbNames(String category) {
        return DB_NAMES.getOrDefault(category, List.of(category));
    }

    // 문장에 이 분야 이름을 직접 썼는지 ex) "교육 관련 정책 있어?" → 교육 true, "대학교 3학년" → 교육 false
    public static boolean mentionedIn(String category, String text) {

        if (category == null || text == null) {
            return false;
        }
        return MENTIONS.getOrDefault(category, List.of()).stream().anyMatch(text::contains);
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
