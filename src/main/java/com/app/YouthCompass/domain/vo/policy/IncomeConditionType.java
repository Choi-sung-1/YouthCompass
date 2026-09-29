package com.app.YouthCompass.domain.vo.policy;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum IncomeConditionType {

    NO_RESTRICTION("0043001", "무관"),
    ANNUAL_INCOME("0043002", "연소득"),
    ETC("0043003", "기타"),

    UNKNOWN(null, "알 수 없음");

    private final String code;
    private final String description;

    IncomeConditionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static IncomeConditionType fromCode(String code) {

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