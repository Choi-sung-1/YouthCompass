package com.app.YouthCompass.policy.domain.eligibility;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SpecialTargetRequirement {

    SME("0014001", "중소기업"),
    WOMAN("0014002", "여성"),
    BASIC_LIVELIHOOD_RECIPIENT("0014003", "기초생활수급자"),
    SINGLE_PARENT_FAMILY("0014004", "한부모가정"),
    DISABLED("0014005", "장애인"),
    FARMER("0014006", "농업인"),
    MILITARY("0014007", "군인"),
    LOCAL_TALENT("0014008", "지역인재"),
    OTHER("0014009", "기타"),
    NO_RESTRICTION("0014010", "제한없음"),

    UNKNOWN(null, "알 수 없음");

    private final String code;
    private final String description;

    SpecialTargetRequirement(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SpecialTargetRequirement fromCode(String code) {

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