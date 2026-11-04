package com.app.YouthCompass.policy.external;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// AI를 부르지 않는 검증 로직만 테스트 (스프링 안 띄움)
class PolicyDocumentServiceTest {

    @Test
    void 원문에_없는_서류는_버린다() {

        List<String> result = PolicyDocumentService.clean(
                List.of("주민등록등본", "가족관계증명서"),
                "1. 주민등록등본 1부");

        assertThat(result).containsExactly("주민등록등본");
    }

    @Test
    void 띄어쓰기가_달라도_원문에_있으면_통과하고_괄호_조건은_유지한다() {

        List<String> result = PolicyDocumentService.clean(
                List.of("주민등록등본(최근 1개월 이내)"),
                "○ 주민등록 등본(최근 1개월 이내 발급) 1부");

        assertThat(result).containsExactly("주민등록등본(최근 1개월 이내)");
    }

    @Test
    void 빈_값_중복_너무_긴_값은_제거한다() {

        String tooLong = "가".repeat(61);
        List<String> result = PolicyDocumentService.clean(
                Arrays.asList("신청서", " ", null, "신청서", tooLong),
                "신청서 " + tooLong);

        assertThat(result).containsExactly("신청서");
    }

    @Test
    void 같은_원문은_같은_해시_다른_원문은_다른_해시() {

        assertThat(PolicyDocumentService.hash("신청서")).hasSize(64)
                .isEqualTo(PolicyDocumentService.hash("신청서"))
                .isNotEqualTo(PolicyDocumentService.hash("신청서, 등본"));
    }
}