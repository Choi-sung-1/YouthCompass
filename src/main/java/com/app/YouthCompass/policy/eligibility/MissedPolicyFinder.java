package com.app.YouthCompass.policy.eligibility;

import com.app.YouthCompass.policy.PolicyService;
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
import com.app.YouthCompass.policy.dto.MissedPolicyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus.NOT_MATCH;

// ============================================================
// 조건 하나 차이로 놓친 정책 찾기 (추천 카드 강화 7번)
//  - 신청 가능한 정책 중 판정에서 NOT_MATCH가 정확히 1개인 정책
//  - 찾는 분야·키워드가 있으면 관련 있는 정책만 (관련 없는 정책은 노이즈)
//  - 걸린 조건과 "이러면 신청할 수 있어요" 안내 문구를 함께 돌려줌
//  - 호출: RecommendResultService (B) — 시그니처는 팀 약속이라 바꾸지 않음
// ============================================================
@Component
@RequiredArgsConstructor
public class MissedPolicyFinder {

    // 안내 문구에 나열할 최대 개수 (넘으면 "등")
    private static final int NAME_LIMIT = 3;

    private final PolicyService policyService;
    private final PolicyEligibilityService eligibilityService;

    // 정렬용: 판정 결과 + 관련도
    private record Candidate(PolicyEvaluationResult result, int relevance) {}

    public List<MissedPolicyDTO> find(UserPolicyProfileVO profile, PolicySearchIntent intent, int limit) {

        if (profile == null || limit <= 0) {
            return List.of();
        }

        boolean hasIntent = intent != null
                && (notBlank(intent.getCategory()) || notBlank(intent.getKeyword()));

        List<Candidate> candidates = new ArrayList<>();

        for (PolicyVO policy : policyService.findOpenPolicies()) {

            PolicyEvaluationResult result = eligibilityService.evaluate(profile, policy);
            if (result.getNotMatchCount() != 1) {
                continue;
            }

            int relevance = relevance(policy, intent);
            if (hasIntent && relevance == 0) {
                continue;
            }
            candidates.add(new Candidate(result, relevance));
        }

        // 관련도 ↓ → 확인 필요 조건 ↑ → 자격 점수 ↓ (추천 정렬과 같은 순서)
        candidates.sort(Comparator.comparingInt(Candidate::relevance).reversed()
                .thenComparingInt(c -> c.result().getUnknownCount())
                .thenComparing(Comparator.comparingInt(
                        (Candidate c) -> c.result().getSpecificMatchScore()).reversed()));

        List<MissedPolicyDTO> missed = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (missed.size() >= limit) {
                break;
            }
            describeNotMatch(candidate.result(), profile).ifPresent(missed::add);        }
        return missed;
    }

    // =========================
    // 걸린 조건 → 안내 문구
    // =========================
    // 안 맞는 조건 1개 → 조건 이름 + 안내 문구 (같은 패키지의 RecheckService도 같은 문구를 씀)
    Optional<MissedPolicyDTO> describeNotMatch(PolicyEvaluationResult r, UserPolicyProfileVO profile) {
        PolicyVO p = r.getPolicy();
        String condition;
        String hint;

        if (r.getAgeResult() == NOT_MATCH) {
            condition = "나이";
            hint = ageHint(p);
        } else if (r.getRegionResult() == NOT_MATCH) {
            condition = "지역";
            hint = regionHint(p, profile);
        } else if (r.getSchoolResult() == NOT_MATCH) {
            condition = "학력";
            hint = codesHint("학력이 ", p.getPolicySchoolCodes(), SchoolRequirement::fromCode,
                    SchoolRequirement::getDescription, "이면 신청할 수 있어요",
                    SchoolRequirement.NO_RESTRICTION, SchoolRequirement.UNKNOWN);
        } else if (r.getJobResult() == NOT_MATCH) {
            condition = "취업 상태";
            hint = codesHint("취업 상태가 ", p.getPolicyJobCodes(), JobRequirement::fromCode,
                    JobRequirement::getDescription, "이면 신청할 수 있어요",
                    JobRequirement.NO_RESTRICTION, JobRequirement.UNKNOWN);
        } else if (r.getMarriageResult() == NOT_MATCH) {
            condition = "혼인 여부";
            hint = codesHint("", p.getPolicyMarriageStatusCodes(), MarriageRequirement::fromCode,
                    MarriageRequirement::getDescription, "이면 신청할 수 있어요",
                    MarriageRequirement.NO_RESTRICTION, MarriageRequirement.UNKNOWN);
        } else if (r.getIncomeResult() == NOT_MATCH) {
            condition = "소득";
            hint = incomeHint(p);
        } else if (r.getMajorResult() == NOT_MATCH) {
            condition = "전공";
            hint = codesHint("전공이 ", p.getPolicyMajorCodes(), MajorRequirement::fromCode,
                    MajorRequirement::getDescription, "이면 신청할 수 있어요",
                    MajorRequirement.NO_RESTRICTION, MajorRequirement.UNKNOWN);
        } else if (r.getSpecialTargetResult() == NOT_MATCH) {
            condition = "지원 대상";
            hint = codesHint("", p.getPolicySpecialTargetCodes(), SpecialTargetRequirement::fromCode,
                    SpecialTargetRequirement::getDescription, "에 해당하면 신청할 수 있어요",
                    SpecialTargetRequirement.NO_RESTRICTION, SpecialTargetRequirement.UNKNOWN);
        } else {
            return Optional.empty();
        }

        // 안내 문구를 못 만들면(코드 해석 불가 등) 보여주지 않음 → 근거 없는 안내 방지
        if (hint == null) {
            return Optional.empty();
        }
        return Optional.of(new MissedPolicyDTO(p.getPolicyId(), p.getPolicyName(), condition, hint));
    }

    // 나이 0·null = 제한 없음 (판정 쪽과 같은 기준)
    private String ageHint(PolicyVO p) {

        Integer min = positive(p.getPolicyMinAge());
        Integer max = positive(p.getPolicyMaxAge());

        if (min != null && max != null) return "만 " + min + "~" + max + "세가 신청할 수 있어요";
        if (min != null) return "만 " + min + "세 이상이 신청할 수 있어요";
        if (max != null) return "만 " + max + "세 이하가 신청할 수 있어요";
        return null;
    }

    // 정책 금액은 만원 단위
    private String incomeHint(PolicyVO p) {

        Long min = positive(p.getPolicyIncomeMinAmount());
        Long max = positive(p.getPolicyIncomeMaxAmount());

        if (min != null && max != null) return String.format("연소득이 %,d~%,d만원이면 신청할 수 있어요", min, max);
        if (max != null) return String.format("연소득이 %,d만원 이하이면 신청할 수 있어요", max);
        if (min != null) return String.format("연소득이 %,d만원 이상이면 신청할 수 있어요", min);
        return null;
    }

    // 같은 시·도 안에서 시·군이 달라 걸렸으면 시·군 이름, 아니면 시·도 이름으로 안내
    private String regionHint(PolicyVO p, UserPolicyProfileVO u) {

        String codes = p.getPolicyRegionCodes();
        if (codes == null || codes.isBlank()) {
            return null;
        }

        Region userRegion = u.getRegion();
        if (userRegion == null && u.getSigunguCode() != null) {
            userRegion = Region.fromCode(u.getSigunguCode()).orElse(null);
        }

        Set<String> sidoNames = new LinkedHashSet<>();
        Set<String> unitNamesInMySido = new LinkedHashSet<>();

        for (String raw : codes.split(",")) {

            String code = raw.trim();
            Optional<Region> region = Region.fromCode(code);
            if (code.isEmpty() || region.isEmpty()) {
                continue;
            }

            sidoNames.add(region.get().getRegionName());
            if (region.get() == userRegion) {
                SigunguRegistry.fromCode(code).ifPresent(unit -> unitNamesInMySido.add(unit.unitName()));
            }
        }

        if (!unitNamesInMySido.isEmpty()) {
            return joinLimited(new ArrayList<>(unitNamesInMySido)) + " 거주자가 신청할 수 있어요";
        }
        if (sidoNames.isEmpty()) {
            return null;
        }
        return joinLimited(new ArrayList<>(sidoNames)) + " 거주자가 신청할 수 있어요";
    }

    // "0049005,0049006" → "학력이 대학 재학·대학 예정이면 신청할 수 있어요"
    @SafeVarargs
    private <E extends Enum<E>> String codesHint(String prefix, String codes,
                                                 Function<String, E> fromCode,
                                                 Function<E, String> describe,
                                                 String suffix,
                                                 E... exclude) {

        if (codes == null || codes.isBlank()) {
            return null;
        }

        Set<E> skip = Set.of(exclude);
        List<String> names = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(fromCode)
                .filter(value -> !skip.contains(value))
                .distinct()
                .map(describe)
                .toList();

        if (names.isEmpty()) {
            return null;
        }
        return prefix + joinLimited(names) + suffix;
    }

    private String joinLimited(List<String> names) {
        if (names.size() <= NAME_LIMIT) {
            return String.join("·", names);
        }
        return String.join("·", names.subList(0, NAME_LIMIT)) + " 등";
    }

    // =========================
    // 관련도: 추천 정렬(PolicyRecommendationService.calculateRelevanceScore)과 같은 기준
    //         그쪽 기준이 바뀌면 여기도 같이 바꿀 것
    // =========================
    private int relevance(PolicyVO p, PolicySearchIntent intent) {

        if (intent == null) {
            return 0;
        }

        int score = 0;
        String category = intent.getCategory();
        String keyword = intent.getKeyword();

        if (notBlank(category) && contains(p.getPolicyLargeCategory(), category)) {
            score += 3;
        }
        if (notBlank(keyword)) {
            if (contains(p.getPolicyMediumCategory(), keyword)) score += 2;
            if (contains(p.getPolicyKeywords(), keyword)) score += 2;
            if (contains(p.getPolicyName(), keyword)) score += 3;
        }
        return score;
    }

    private boolean contains(String text, String word) {
        return text != null && text.contains(word);
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static <T extends Number> T positive(T value) {
        return value != null && value.longValue() > 0 ? value : null;
    }
}