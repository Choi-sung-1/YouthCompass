package com.app.YouthCompass.policy.recommendation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// AI 문장의 [이름] 자리 → 실제 이름 (이름은 AI에 보내지 않음)
class PolicyInsightNameTest {

    @Test
    void 이름_자리를_회원_이름으로_바꾼다() {
        String reason = "[이름]님에게 이 정책을 추천하는 이유는 월세 부담이 크다고 하셨기 때문이에요. [이름]님처럼 혼자 사는 청년이 대상이에요.";

        assertThat(PolicyInsightService.fillName(reason, PolicyInsightService.displayName("최성원")))
                .isEqualTo("최성원님에게 이 정책을 추천하는 이유는 월세 부담이 크다고 하셨기 때문이에요. 최성원님처럼 혼자 사는 청년이 대상이에요.");
    }

    @Test
    void 이름이_없으면_회원으로_부른다() {
        assertThat(PolicyInsightService.fillName("[이름]님에게", PolicyInsightService.displayName(null)))
                .isEqualTo("회원님에게");
        assertThat(PolicyInsightService.fillName(null, "회원")).isNull();
    }

    @Test
    void 프롬프트_데이터의_중괄호를_소괄호로_바꾼다() {
        assertThat(PolicyInsightService.noBraces("월세 {최대 20만원} 지원")).isEqualTo("월세 (최대 20만원) 지원");
        assertThat(PolicyInsightService.noBraces(null)).isNull();
    }
}
