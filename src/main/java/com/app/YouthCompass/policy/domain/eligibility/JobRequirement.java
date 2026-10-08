package com.app.YouthCompass.policy.domain.eligibility;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum JobRequirement {

    EMPLOYEE("0013001", "재직자"),
    SELF_EMPLOYED("0013002", "자영업자"),
    UNEMPLOYED("0013003", "미취업자"),
    FREELANCER("0013004", "프리랜서"),
    DAILY_WORKER("0013005", "일용근로자"),
    ENTREPRENEUR("0013006", "(예비)창업자"),
    SHORT_TERM_WORKER("0013007", "단기근로자"),
    FARMER("0013008", "영농종사자"),
    OTHER("0013009", "기타"),
    NO_RESTRICTION("0013010", "제한없음"),

    UNKNOWN(null, "알 수 없음");

    private final String code;
    private final String description;

    JobRequirement(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static JobRequirement fromCode(String code) {

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