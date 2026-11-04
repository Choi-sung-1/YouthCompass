package com.app.YouthCompass.history;

import com.app.YouthCompass.history.domain.RecommendHistoryVO;
import com.app.YouthCompass.history.domain.RecommendItemVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // 테스트마다 롤백 → 로컬 DB에 흔적 안 남음
class RecommendHistoryMapperTest {

    @Autowired
    private RecommendHistoryMapper historyMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // 항목 개수 확인용

    private static final Long MEMBER_ID = 999999L;
    private static final Long OTHER_MEMBER_ID = 999998L;

    private RecommendHistoryVO history(String sentence) {
        RecommendHistoryVO vo = new RecommendHistoryVO();
        vo.setMemberId(MEMBER_ID);
        vo.setSentence(sentence);
        vo.setProfileJson("{\"age\":25}");
        return vo;
    }

    private RecommendItemVO item(Long historyId, int rankNo, Long policyId) {
        RecommendItemVO vo = new RecommendItemVO();
        vo.setHistoryId(historyId);
        vo.setRankNo(rankNo);
        vo.setPolicyId(policyId);
        vo.setReason("추천 이유 " + rankNo);
        vo.setEffect("기대 효과 " + rankNo);
        return vo;
    }

    @Test
    void 기록을_저장하면_생성된_번호가_채워진다() {
        RecommendHistoryVO history = history("수원 사는 25살 취준생");

        assertThat(historyMapper.insertHistory(history)).isEqualTo(1);
        assertThat(history.getHistoryId()).isNotNull();
    }

    @Test
    void 항목_여러개를_한번에_저장한다() {
        RecommendHistoryVO history = history("월세 지원 받고 싶어요");
        historyMapper.insertHistory(history);

        int saved = historyMapper.insertItems(List.of(
                item(history.getHistoryId(), 1, 999999L),
                item(history.getHistoryId(), 2, 999998L)
        ));

        assertThat(saved).isEqualTo(2);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TBL_RECOMMEND_ITEM WHERE HISTORY_ID = ?",
                Integer.class, history.getHistoryId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 내_기록_목록은_최근_것부터_나온다() {
        RecommendHistoryVO first = history("첫 번째 추천");
        RecommendHistoryVO second = history("두 번째 추천");
        historyMapper.insertHistory(first);
        historyMapper.insertHistory(second);

        List<RecommendHistoryVO> list = historyMapper.selectHistoriesByMemberId(MEMBER_ID);

        assertThat(list).extracting(RecommendHistoryVO::getSentence)
                .containsExactly("두 번째 추천", "첫 번째 추천");
    }

    @Test
    void 기록_한건은_내_것만_조회되고_남의_번호면_비어있다() {
        RecommendHistoryVO history = history("내 기록");
        historyMapper.insertHistory(history);

        assertThat(historyMapper.selectHistory(history.getHistoryId(), MEMBER_ID))
                .isPresent()
                .get()
                .extracting(RecommendHistoryVO::getSentence)
                .isEqualTo("내 기록");

        assertThat(historyMapper.selectHistory(history.getHistoryId(), OTHER_MEMBER_ID)).isEmpty();
    }
}