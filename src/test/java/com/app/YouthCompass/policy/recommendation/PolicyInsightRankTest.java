package com.app.YouthCompass.policy.recommendation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// AI 순위 응답 검증: 후보에 없는 id·중복·null 제거, 최대 topN개
class PolicyInsightRankTest {

    private final Map<Long, String> candidates = Map.of(1L, "a", 2L, "b", 3L, "c");

    @Test
    void 후보에_없는_id와_중복을_빼고_순서를_유지한다() {
        PolicyInsightService.RankResponse response =
                new PolicyInsightService.RankResponse(Arrays.asList(3L, 99L, 1L, 3L, null, 2L));

        assertThat(PolicyInsightService.validateRank(response, candidates, 10)).containsExactly(3L, 1L, 2L);
    }

    @Test
    void topN개까지만_남긴다() {
        PolicyInsightService.RankResponse response = new PolicyInsightService.RankResponse(List.of(2L, 1L, 3L));

        assertThat(PolicyInsightService.validateRank(response, candidates, 2)).containsExactly(2L, 1L);
    }

    @Test
    void 응답이_비면_빈_목록_규칙_기반으로_대체된다() {
        assertThat(PolicyInsightService.validateRank(null, candidates, 10)).isEmpty();
        assertThat(PolicyInsightService.validateRank(new PolicyInsightService.RankResponse(null), candidates, 10)).isEmpty();
    }
}
