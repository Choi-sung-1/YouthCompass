package com.app.YouthCompass.history;

import com.app.YouthCompass.history.domain.RecommendHistoryVO;
import com.app.YouthCompass.history.domain.RecommendItemVO;
import com.app.YouthCompass.history.dto.RecommendHistoryDetailDTO;
import com.app.YouthCompass.history.dto.RecommendHistoryItemDTO;
import com.app.YouthCompass.policy.PolicyMapper;
import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.policy.domain.eligibility.SpecialTargetRequirement;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.dto.PolicyListItemDTO;
import com.app.YouthCompass.policy.dto.RecommendResponseDTO;
import com.app.YouthCompass.policy.dto.RecommendedPolicyDTO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import com.app.YouthCompass.policy.recommendation.RecommendReasonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendHistoryService {

    // TBL_RECOMMEND_ITEM.REASON / EFFECT 컬럼 길이 (VARCHAR(500))
    private static final int TEXT_LIMIT = 500;

    private final RecommendHistoryMapper historyMapper;
    private final JsonMapper jsonMapper;
    private final TransactionTemplate transactionTemplate;

    // 기록 상세 카드: 추천 화면과 같은 판정·문장 (A의 정책 조회·판정·이유 문장 — 호출만)
    private final PolicyMapper policyMapper;
    private final PolicyEligibilityService eligibilityService;
    private final RecommendReasonBuilder reasonBuilder;

    // 생성자 주입 (TransactionTemplate은 트랜잭션 매니저로 직접 만듦)
    public RecommendHistoryService(RecommendHistoryMapper historyMapper,
                                   JsonMapper jsonMapper,
                                   PlatformTransactionManager transactionManager,
                                   PolicyMapper policyMapper,
                                   PolicyEligibilityService eligibilityService,
                                   RecommendReasonBuilder reasonBuilder) {
        this.historyMapper = historyMapper;
        this.jsonMapper = jsonMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.policyMapper = policyMapper;
        this.eligibilityService = eligibilityService;
        this.reasonBuilder = reasonBuilder;
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

    //    추천 기록 삭제 (내 기록일 때만) — 반환: 삭제했으면 true, 남의 기록이거나 없으면 false
    //    항목 + 기록을 한 트랜잭션으로 (항목만 지워지고 기록이 남는 일 없게)
    public boolean delete(Long historyId, Long memberId) {
        if (historyId == null || memberId == null) {
            return false;
        }
        Boolean deleted = transactionTemplate.execute(status -> {
            historyMapper.deleteItems(historyId, memberId);
            return historyMapper.deleteHistory(historyId, memberId) == 1;
        });
        return Boolean.TRUE.equals(deleted);
    }

    //    추천 기록 상세 (내 기록일 때만, 남의 기록이거나 없으면 empty)
    public Optional<RecommendHistoryDetailDTO> findHistoryDetail(Long historyId, Long memberId) {
        if (historyId == null || memberId == null) {
            return Optional.empty();
        }

        // 1) 본인 기록인지 먼저 확인 → 2) 맞을 때만 항목 조회
        return historyMapper.selectHistory(historyId, memberId).map(history -> {
            Map<String, String> conditions = readConditions(history.getProfileJson());
            UserPolicyProfileVO profile = toProfile(conditions);
            PolicySearchIntent intent = toIntent(conditions);

            List<RecommendHistoryItemDTO> items = historyMapper.selectItemsWithPolicy(historyId);
            items.forEach(item -> {
                fillDeadline(item);
                fillCard(item, profile, intent);
            });

            return new RecommendHistoryDetailDTO(
                    history.getHistoryId(),
                    history.getSentence(),
                    history.getCreatedAt(),
                    conditions,
                    items
            );
        });
    }

    //    추천 화면과 같은 카드 (자격 조건·신청 준비·지원 내용 등)
    //    그때 조건으로 지금 정책 정보를 다시 판정 → 정책 정보가 바뀌었으면 그때와 다를 수 있음
    //    추천 이유·기대 효과는 AI 재호출 없이 저장해 둔 문장 사용
    private void fillCard(RecommendHistoryItemDTO item, UserPolicyProfileVO profile, PolicySearchIntent intent) {
        if (item.getPolicyName() == null) {
            return;
        }
        PolicyVO policy = policyMapper.findPolicyById(item.getPolicyId());
        if (policy == null) {
            return;
        }
        PolicyEvaluationResult result = eligibilityService.evaluate(profile, policy);

        RecommendedPolicyDTO card = RecommendedPolicyDTO.from(result);
        card.setReasons(reasonBuilder.reasons(result, profile, intent));
        card.setUnknownReasons(reasonBuilder.unknownReasons(result, profile));
        card.setRecommendReason(item.getReason());
        card.setExpectedEffect(item.getEffect());
        item.setCard(card);
        item.setNotMatchedConditions(notMatched(result));
    }

    //    추천 때는 없던 '맞지 않는 조건' (그 뒤로 정책 조건이 바뀐 경우) → 화면에서 경고
    private List<String> notMatched(PolicyEvaluationResult r) {
        Map<String, EligibilityStatus> byName = new LinkedHashMap<>();
        byName.put("나이", r.getAgeResult());
        byName.put("지역", r.getRegionResult());
        byName.put("학력", r.getSchoolResult());
        byName.put("취업 상태", r.getJobResult());
        byName.put("혼인 여부", r.getMarriageResult());
        byName.put("소득", r.getIncomeResult());
        byName.put("전공", r.getMajorResult());
        byName.put("지원 대상", r.getSpecialTargetResult());

        return byName.entrySet().stream()
                .filter(e -> e.getValue() == EligibilityStatus.NOT_MATCH)
                .map(Map.Entry::getKey)
                .toList();
    }

    //    지금 기준 마감 표시 (A의 PolicyListItemDTO 계산을 그대로 사용 → 정책 목록·찜 목록과 같은 표시)
    private void fillDeadline(RecommendHistoryItemDTO item) {
        if (item.getPolicyName() == null) {
            item.setDeadlineLabel("정책 정보 없음");
            return;
        }
        PolicyListItemDTO card = PolicyListItemDTO.from(PolicyVO.builder()
                .policyId(item.getPolicyId())
                .policyName(item.getPolicyName())
                .policyApplyPeriodCode(item.getPolicyApplyPeriodCode())
                .policyApplyEndDate(item.getPolicyApplyEndDate())
                .build());

        item.setDeadlineLabel(card.getDeadlineLabel());
        item.setClosingSoon(card.isClosingSoon());
        item.setClosed(card.isClosed());
    }

    //    저장해 둔 조건 JSON → Map (LinkedHashMap이라 저장 순서 유지, 읽기 실패하면 빈 Map)
    @SuppressWarnings("unchecked")
    private Map<String, String> readConditions(String profileJson) {
        if (profileJson == null || profileJson.isBlank()) {
            return Map.of();
        }
        try {
            return jsonMapper.readValue(profileJson, LinkedHashMap.class);
        } catch (RuntimeException e) {
            log.warn("추천 기록 조건 JSON 읽기 실패 (조건 칩 없이 표시)", e);
            return Map.of();
        }
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

    //    toProfileJson의 반대: 저장해 둔 조건 → 판정용 조건 (기록 상세에서 자격 조건을 다시 판정할 때 사용)
    //    toProfileJson의 키·표시 형식을 바꾸면 여기도 같이 바꿀 것 (RecommendHistoryServiceTest 왕복 테스트로 확인)
    //    읽을 수 없는 값은 null = 모름 → 판정에서 확인 필요로 나옴
    UserPolicyProfileVO toProfile(Map<String, String> c) {
        Region region = byLabel(Region.values(), Region::getRegionName, c.get("지역"));
        String sidoCode = region == null ? null : region.getRegionCodes().get(0);
        Long age = parseNumber(c.get("나이"));

        return UserPolicyProfileVO.builder()
                .age(age == null ? null : age.intValue())
                .region(region)
                .sigunguCode(c.get("시·군") == null ? null
                        : SigunguRegistry.findByName(sidoCode, c.get("시·군")).map(SigunguRegistry.Unit::unitCode).orElse(null))
                .school(byLabel(SchoolRequirement.values(), SchoolRequirement::getDescription, c.get("학력")))
                .job(byLabel(JobRequirement.values(), JobRequirement::getDescription, c.get("취업")))
                .marriage(byLabel(MarriageRequirement.values(), MarriageRequirement::getDescription, c.get("혼인")))
                .annualIncome(parseNumber(c.get("연소득")))
                .major(byLabel(MajorRequirement.values(), MajorRequirement::getDescription, c.get("전공")))
                .specialTargets(toTargets(c.get("지원 대상")))
                .build();
    }

    PolicySearchIntent toIntent(Map<String, String> c) {
        return PolicySearchIntent.builder()
                .category(c.get("찾는 분야"))
                .keyword(c.get("찾는 지원"))
                .build();
    }

    // "해당 없음" = 빈 Set (해당 없다고 말함), 없으면 null (언급 없음)
    private Set<SpecialTargetRequirement> toTargets(String text) {
        if (text == null) {
            return null;
        }
        Set<SpecialTargetRequirement> targets = EnumSet.noneOf(SpecialTargetRequirement.class);
        if (text.equals("해당 없음")) {
            return targets;
        }
        for (String label : text.split(", ")) {
            SpecialTargetRequirement t = byLabel(SpecialTargetRequirement.values(), SpecialTargetRequirement::getDescription, label);
            if (t != null) {
                targets.add(t);
            }
        }
        return targets;
    }

    // 화면 표시 이름 → enum (못 찾으면 null)
    private <E extends Enum<E>> E byLabel(E[] values, Function<E, String> label, String text) {
        if (text == null) {
            return null;
        }
        return Arrays.stream(values).filter(v -> text.equals(label.apply(v))).findFirst().orElse(null);
    }

    // "25세" → 25, "2,400만원" → 2400 (숫자가 없으면 null)
    private Long parseNumber(String text) {
        if (text == null) {
            return null;
        }
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : Long.valueOf(digits);
    }

    //    컬럼 길이를 넘는 AI 문장은 잘라서 저장 (넘으면 INSERT 실패)
    private String cut(String text) {
        if (text == null || text.length() <= TEXT_LIMIT) {
            return text;
        }
        return text.substring(0, TEXT_LIMIT - 1) + "…";
    }
}