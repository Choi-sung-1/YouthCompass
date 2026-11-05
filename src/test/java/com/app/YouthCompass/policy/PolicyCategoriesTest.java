package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.recommendation.PolicyCategories;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyCategoriesTest {

    @Test
    void 분야_이름을_직접_쓰면_언급으로_본다() {
        assertThat(PolicyCategories.mentionedIn("교육", "교육 관련 정책 있어?")).isTrue();
        assertThat(PolicyCategories.mentionedIn("복지문화", "문화생활 지원 있나요")).isTrue();
        assertThat(PolicyCategories.mentionedIn("일자리", "일자리 정책 알려줘")).isTrue();
    }

    @Test
    void 신분만_말하면_분야_언급이_아니다() {
        assertThat(PolicyCategories.mentionedIn("교육", "대학교 3학년인데 받을 수 있는 정책?")).isFalse();
        assertThat(PolicyCategories.mentionedIn("일자리", "취업 준비하는 26살이에요")).isFalse();
        assertThat(PolicyCategories.mentionedIn(null, "교육")).isFalse();
    }
}
