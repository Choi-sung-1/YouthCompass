package com.app.YouthCompass.domain.vo.policy;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MarriageRequirement {

    MARRIED("0055001", "기혼"),
    SINGLE("0055002", "미혼"),
    NO_RESTRICTION("0055003", "제한없음"),

    UNKNOWN(null, "알 수 없음");

    private final String code;
    private final String description;

    MarriageRequirement(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MarriageRequirement fromCode(String code) {

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