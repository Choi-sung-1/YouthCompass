package com.app.YouthCompass.domain.vo.policy;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MajorRequirement {

    HUMANITIES("0011001", "인문계열"),
    SOCIAL_SCIENCE("0011002", "사회계열"),
    BUSINESS_ECONOMICS("0011003", "상경계열"),
    NATURAL_SCIENCE("0011004", "이학계열"),
    ENGINEERING("0011005", "공학계열"),
    ARTS_SPORTS("0011006", "예체능계열"),
    AGRICULTURE("0011007", "농산업계열"),
    OTHER("0011008", "기타"),
    NO_RESTRICTION("0011009", "제한없음"),

    UNKNOWN(null, "알 수 없음");

    private final String code;
    private final String description;

    MajorRequirement(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MajorRequirement fromCode(String code) {

        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(value -> value.code != null)
                .filter(value -> value.code.equals(code.trim()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}