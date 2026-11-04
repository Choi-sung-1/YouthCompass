package com.app.YouthCompass.policy.dto;

import java.util.List;

// 다시 판정에서 사용자에게 물어볼 질문 하나
//  field : ConditionRequestDTO 필드 이름 (age, region, sigungu, school, job, marriage, annualIncome, major, specialTargets)
//  type  : "select" | "number" | "text" | "checkbox"
public record RecheckQuestionDTO(String field, String label, String type, List<Option> options) {

    // 선택지 (value = enum 이름, label = 화면 문구)
    public record Option(String value, String label) {}

    public static RecheckQuestionDTO number(String field, String label) {
        return new RecheckQuestionDTO(field, label, "number", List.of());
    }

    public static RecheckQuestionDTO text(String field, String label) {
        return new RecheckQuestionDTO(field, label, "text", List.of());
    }

    public static RecheckQuestionDTO select(String field, String label, List<Option> options) {
        return new RecheckQuestionDTO(field, label, "select", options);
    }

    public static RecheckQuestionDTO checkbox(String field, String label, List<Option> options) {
        return new RecheckQuestionDTO(field, label, "checkbox", options);
    }
}