package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyText;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 공공데이터 빈 값·URL 정리 (로컬 DB에서 실제로 본 값들)
class PolicyTextTest {

    @Test
    void 빈_값과_형식만_채운_값은_null() {
        assertThat(PolicyText.text(null)).isNull();
        assertThat(PolicyText.text("")).isNull();
        assertThat(PolicyText.text("   \n")).isNull();
        assertThat(PolicyText.text("　")).isNull();   // 전각 공백
        assertThat(PolicyText.text("-")).isNull();
        assertThat(PolicyText.text(" 해당없음 ")).isNull();
        assertThat(PolicyText.text(".")).isNull();
        assertThat(PolicyText.text(" 온라인 신청 ")).isEqualTo("온라인 신청");
    }

    @Test
    void URL은_링크로_쓸_수_있는_것만() {
        assertThat(PolicyText.url("https://www.work24.go.kr")).isEqualTo("https://www.work24.go.kr");
        assertThat(PolicyText.url("www.bokjiro.go.kr")).isEqualTo("https://www.bokjiro.go.kr");
        assertThat(PolicyText.url("plus.gov.kr/path?a=1")).isEqualTo("https://plus.gov.kr/path?a=1");
        assertThat(PolicyText.url("　https://www.dangjin.go.kr/atc.do")).isEqualTo("https://www.dangjin.go.kr/atc.do");
        assertThat(PolicyText.url("https://a.kr https://b.kr")).isEqualTo("https://a.kr");
        assertThat(PolicyText.url("전화문의")).isNull();
        assertThat(PolicyText.url("-")).isNull();
        assertThat(PolicyText.url("")).isNull();
        assertThat(PolicyText.url("javascript:alert(1)")).isNull();
    }

    @Test
    void 신청_URL이_없으면_참고_URL() {
        PolicyVO policy = PolicyVO.builder()
                .policyApplicationUrl(" ")
                .policyReferenceUrl("www.usc.go.kr")
                .build();
        assertThat(PolicyText.applyUrl(policy)).isEqualTo("https://www.usc.go.kr");

        policy.setPolicyReferenceUrl("");
        assertThat(PolicyText.applyUrl(policy)).isNull();
    }
}
