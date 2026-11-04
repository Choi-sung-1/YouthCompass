package com.app.YouthCompass.history;

import com.app.YouthCompass.history.domain.RecommendHistoryVO;
import com.app.YouthCompass.history.domain.RecommendItemVO;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.policy.domain.eligibility.SpecialTargetRequirement;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.dto.RecommendResponseDTO;
import com.app.YouthCompass.policy.dto.RecommendedPolicyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendHistoryService {

    // TBL_RECOMMEND_ITEM.REASON / EFFECT 컬럼 길이 (VARCHAR(500))
    private static final int TEXT_LIMIT = 500;

    private final RecommendHistoryMapper historyMapper;
    private final JsonMapper jsonMapper;
    private final TransactionTemplate transactionTemplate;

    // 생성자 주입 (TransactionTemplate은 트랜잭션 매니저로 직접 만듦)
    public RecommendHistoryService(RecommendHistoryMapper historyMapper,
                                   JsonMapper jsonMapper,
                                   PlatformTransactionManager transactionManager) {
        this.historyMapper = historyMapper;
        this.jsonMapper = jsonMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    //    추천 결과 저장 (PolicyController에서 추천 직후 한 줄로 호출)
    //    반환: 생성된 historyId / 저장하지 않았거나 실패하면 null
    //    실패해도 예외를 던지지 않음 → 기록 저장 때문에 추천 결과 화면이 깨지지 않게
    public Long save(Long memberId, String sentence, RecommendResponseDTO result) {
        if (memberId == null || result == null || result.getPolicies() == null) {
            return null;
        }

        try {
            // 기록 1행 + 항목 N행을 한 트랜잭션으로 (중간에 실패하면 둘 다 취소)
            return transactionTemplate.execute(status -> insertHistoryAndItems(memberId, sentence, result));
        } catch (RuntimeException e) {
            log.warn("추천 기록 저장 실패 memberId={} (추천 결과 화면은 그대로 표시)", memberId, e);
            return null;
        }
    }

    //    내 추천 기록 목록 (최근 순, 비로그인이면 빈 리스트)
    public List<RecommendHistoryVO> findHistories(Long memberId) {
        if (memberId == null) {
            return List.of();
        }
        return historyMapper.selectHistoriesByMemberId(memberId);
    }

    //    실제 저장 (transactionTemplate 안에서만 호출)
    private Long insertHistoryAndItems(Long memberId, String sentence, RecommendResponseDTO result) {

        // 항목부터 만들어 봄 → 저장할 정책이 하나도 없으면 기록도 남기지 않음
        List<RecommendItemVO> items = new ArrayList<>();
        int rankNo = 1;
        for (RecommendedPolicyDTO policy : result.getPolicies()) {
            if (policy.getPolicyId() == null) {
                continue;
            }
            RecommendItemVO item = new RecommendItemVO();
            item.setRankNo(rankNo++);
            item.setPolicyId(policy.getPolicyId());
            item.setReason(cut(policy.getRecommendReason()));
            item.setEffect(cut(policy.getExpectedEffect()));
            items.add(item);
        }
        if (items.isEmpty()) {
            return null;
        }

        RecommendHistoryVO history = new RecommendHistoryVO();
        history.setMemberId(memberId);
        history.setSentence(sentence);
        history.setProfileJson(toProfileJson(result.getProfile(), result.getIntent()));
        historyMapper.insertHistory(history);   // → history.historyId 채워짐

        items.forEach(item -> item.setHistoryId(history.getHistoryId()));
        historyMapper.insertItems(items);

        log.debug("추천 기록 저장 memberId={}, historyId={}, 항목 {}개",
                memberId, history.getHistoryId(), items.size());
        return history.getHistoryId();
    }

    //    AI가 이해한 조건 → 화면 표시용 JSON {"나이":"25세","지역":"경기도",...} (값이 있는 것만, 순서 유지)
    private String toProfileJson(UserPolicyProfileVO profile, PolicySearchIntent intent) {
        Map<String, String> summary = new LinkedHashMap<>();

        if (profile != null) {
            if (profile.getAge() != null) {
                summary.put("나이", profile.getAge() + "세");
            }
            if (profile.getRegion() != null) {
                summary.put("지역", profile.getRegion().getRegionName());
            }
            if (profile.getSigunguCode() != null) {
                summary.put("시·군", SigunguRegistry.fromUnitCode(profile.getSigunguCode())
                        .map(SigunguRegistry.Unit::unitName)
                        .orElse(profile.getSigunguCode()));
            }
            if (profile.getSchool() != null) {
                summary.put("학력", profile.getSchool().getDescription());
            }
            if (profile.getJob() != null) {
                summary.put("취업", profile.getJob().getDescription());
            }
            if (profile.getMarriage() != null) {
                summary.put("혼인", profile.getMarriage().getDescription());
            }
            if (profile.getAnnualIncome() != null) {
                summary.put("연소득", String.format("%,d만원", profile.getAnnualIncome()));
            }
            if (profile.getMajor() != null) {
                summary.put("전공", profile.getMajor().getDescription());
            }
            // null = 언급 없음(안 넣음), 빈 목록 = 해당 없음 (추천 화면과 같은 규칙)
            if (profile.getSpecialTargets() != null) {
                summary.put("지원 대상", profile.getSpecialTargets().isEmpty()
                        ? "해당 없음"
                        : profile.getSpecialTargets().stream()
                        .map(SpecialTargetRequirement::getDescription)
                        .collect(Collectors.joining(", ")));
            }
        }

        if (intent != null) {
            if (intent.getCategory() != null) {
                summary.put("찾는 분야", intent.getCategory());
            }
            if (intent.getKeyword() != null) {
                summary.put("찾는 지원", intent.getKeyword());
            }
        }

        return jsonMapper.writeValueAsString(summary);
    }

    //    컬럼 길이를 넘는 AI 문장은 잘라서 저장 (넘으면 INSERT 실패)
    private String cut(String text) {
        if (text == null || text.length() <= TEXT_LIMIT) {
            return text;
        }
        return text.substring(0, TEXT_LIMIT - 1) + "…";
    }
}