package com.app.YouthCompass.policy.domain.model;

import java.util.Set;
import java.util.regex.Pattern;

// 공공데이터 원문 값 정리 (화면에 보여주기 전에 한 번 거침)
// - Thymeleaf th:if 는 빈 문자열 ""도 true 로 봐서, 빈 값을 null 로 바꾸지 않으면 내용 없는 칸·버튼이 보임
// - URL 은 "www.xxx.kr"처럼 http 없이 오면 우리 사이트 안의 주소로 읽혀서 엉뚱한 화면(404)으로 감
public final class PolicyText {

    private PolicyText() {
    }

    // 내용 없이 형식만 채운 값
    private static final Set<String> MEANINGLESS = Set.of("해당없음", "해당 없음", "없음", "-", ".", "미정");

    // 도메인 모양 ex) www.bokjiro.go.kr, plus.gov.kr/path
    private static final Pattern DOMAIN = Pattern.compile("^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+(/\\S*)?$");

    // 빈 값·공백(전각 공백 포함)·"해당없음" 같은 값 → null, 아니면 앞뒤 공백 제거
    public static String text(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() || MEANINGLESS.contains(stripped) ? null : stripped;
    }

    // 링크로 쓸 수 있는 URL 만 남김
    // "https://…" 그대로 / "www.xxx.kr" → "https://www.xxx.kr" / "전화문의"·"-"·빈 값 → null
    public static String url(String value) {
        String text = text(value);
        if (text == null) {
            return null;
        }

        // 여러 개가 공백·줄바꿈으로 이어져 오면 첫 번째만
        String first = text.split("\\s+")[0];

        String lower = first.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return first.length() > "https://".length() ? first : null;
        }
        if (DOMAIN.matcher(first).matches()) {
            return "https://" + first;
        }
        return null;
    }

    // 신청 URL 이 없으면 참고 URL (둘 다 없으면 null)
    public static String applyUrl(PolicyVO policy) {
        String url = url(policy.getPolicyApplicationUrl());
        return url != null ? url : url(policy.getPolicyReferenceUrl());
    }
}
