package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 같은 사업이 새 번호로 다시 등록된 중복 정리 (로컬 DB에서 실제로 본 경우들)
class PolicyDuplicateTest {

    private PolicyVO policy(long id, String externalId, String name, String regionCodes) {
        return PolicyVO.builder()
                .policyId(id)
                .policyExternalId(externalId)
                .policyName(name)
                .policyRegionCodes(regionCodes)
                .build();
    }

    @Test
    void 이름과_지역이_같으면_최근_등록만_남는다() {

        // 975(2026년 등록)·3298(2025년 등록) 청년 주택드림 디딤돌 대출
        PolicyVO newer = policy(975, "20260922005400113546", "청년 주택드림 디딤돌 대출", "11,26");
        PolicyVO older = policy(3298, "20250514005400110811", "청년 주택드림 디딤돌 대출", "11,26");

        assertThat(PolicyService.withoutDuplicates(List.of(older, newer))).containsExactly(newer);
        assertThat(PolicyService.withoutDuplicates(List.of(newer, older))).containsExactly(newer);
    }

    @Test
    void 지역이_다르면_같은_이름이어도_다른_사업() {

        PolicyVO ulsan = policy(1, "20260413000000000001", "전세보증금반환보증 보증료 지원", "31");
        PolicyVO busan = policy(2, "20260413000000000002", "전세보증금반환보증 보증료 지원", "26");

        assertThat(PolicyService.withoutDuplicates(List.of(ulsan, busan))).containsExactly(ulsan, busan);
    }

    @Test
    void 지역_코드_순서와_이름_공백_차이는_무시한다() {

        PolicyVO a = policy(1, "20250101000000000001", "청년 월세 지원", "26,11");
        PolicyVO b = policy(2, "20260101000000000001", "청년  월세지원", "11, 26");

        assertThat(PolicyService.withoutDuplicates(List.of(a, b))).containsExactly(b);
    }

    @Test
    void 이름이_없으면_합치지_않는다() {

        PolicyVO a = policy(1, "20250101000000000001", null, "11");
        PolicyVO b = policy(2, "20260101000000000001", " ", "11");

        assertThat(PolicyService.withoutDuplicates(List.of(a, b))).containsExactly(a, b);
    }
}
