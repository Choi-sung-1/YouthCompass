package com.app.YouthCompass.history;

import com.app.YouthCompass.history.domain.RecommendHistoryVO;
import com.app.YouthCompass.history.domain.RecommendItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RecommendHistoryMapper {

    // 추천 기록 1행 저장 (INSERT 후 history.historyId 에 생성된 PK가 채워짐)
    int insertHistory(RecommendHistoryVO history);

    // 추천 항목 여러 행을 한 번에 저장 (빈 리스트는 SQL 오류 → Service에서 막음)
    int insertItems(@Param("items") List<RecommendItemVO> items);

    // 내 추천 기록 목록 (최근 순)
    List<RecommendHistoryVO> selectHistoriesByMemberId(Long memberId);

    // 추천 기록 한 건 (내 기록일 때만 조회됨 → 남의 기록 번호를 넣으면 빈 결과)
    Optional<RecommendHistoryVO> selectHistory(@Param("historyId") Long historyId,
                                               @Param("memberId") Long memberId);
}