package com.app.YouthCompass.domain.vo.policy;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SchoolRequirement {

    BELOW_HIGH_SCHOOL("0049001", "고졸 미만"),
    HIGH_SCHOOL_STUDENT("0049002", "고교 재학"),
    HIGH_SCHOOL_EXPECTED_GRADUATE("0049003", "고졸 예정"),
    HIGH_SCHOOL_GRADUATE("0049004", "고교 졸업"),
    UNIVERSITY_STUDENT("0049005", "대학 재학"),
    UNIVERSITY_EXPECTED_GRADUATE("0049006", "대학 예정"),
    UNIVERSITY_GRADUATE("0049007", "대학 졸업"),
    MASTER_OR_DOCTOR("0049008", "석·박사"),
    OTHER("0049009", "기타"),
    NO_RESTRICTION("0049010", "제한없음"),

    UNKNOWN(null, "알 수 없음");

    private final String code;
    private final String description;

    SchoolRequirement(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SchoolRequirement fromCode(String code) {
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