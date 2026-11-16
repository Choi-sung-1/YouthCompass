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
    void 행정구역_개편으로_코드만_바뀐_같은_지역이면_같은_사업() {

        // 977(2026년 등록)·3859(2024년 등록) 청년전용 저리대출상품 운영
        // 새 등록은 화성시가 구로 나뉜 코드(41591~41597)와 인천 신설 구 코드(28155), 옛 등록은 화성시 41590
        PolicyVO newer = policy(977, "20260922005400113544", "청년전용 저리대출상품 운영", "28125,28155,41570,41591,41593,41595,41597");
        PolicyVO older = policy(3859, "20240109005400100002", "청년전용 저리대출상품 운영", "28125,41570,41590");

        assertThat(PolicyService.withoutDuplicates(List.of(older, newer))).containsExactly(newer);
    }

    @Test
    void 같은_도의_다른_시는_같은_이름이어도_다른_사업() {

        PolicyVO pyeongtaek = policy(1, "20260413000000000001", "청년 면접정장 무료대여", "41220");
        PolicyVO suwon = policy(2, "20260413000000000002", "청년 면접정장 무료대여", "41111");

        assertThat(PolicyService.withoutDuplicates(List.of(pyeongtaek, suwon))).containsExactly(pyeongtaek, suwon);
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
