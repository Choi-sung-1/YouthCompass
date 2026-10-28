package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.IncomeConditionType;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus.MATCH;
import static com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus.UNKNOWN;

// 판정 결과(PolicyEvaluationResult)를 사람이 읽는 문장으로 바꾼다
//  - reasons        : 추천 이유 (찾는 분야·키워드와 관련, 실제로 맞은 조건)
//  - unknownReasons : 확인이 필요한 조건 (사용자 정보 부족 / 공고 확인 필요)
//
// "실제 조건이 있는지" 판단 기준은 PolicyEligibilityService의 점수 계산(hasReal...)과 같게 맞춤
// → 조건이 없는 항목(제한없음)은 "맞아요"라고 말하지 않는다
@Component
public class RecommendReasonBuilder {

    // 지역 코드가 이보다 많으면 전국 대상으로 보고 지역 조건으로 치지 않음 (점수 계산과 같은 기준)
    private static final int NATIONWIDE_REGION_CODE_COUNT = 100;

    // 소득 기타 조건 문장이 너무 길면 자름
    private static final int ETC_TEXT_LIMIT = 80;

    // =========================
    // 추천 이유
    // =========================
    public List<String> reasons(
            PolicyEvaluationResult result,
            UserPolicyProfileVO user,
            PolicySearchIntent intent
    ) {

        PolicyVO policy = result.getPolicy();
        UserPolicyProfileVO u = (user != null) ? user : new UserPolicyProfileVO();
        List<String> reasons = new ArrayList<>();

        // 1. 찾는 분야·키워드 (PolicyRecommendationService의 관련도 계산과 같은 비교)
        if (intent != null) {

            String category = intent.getCategory();
            if (category != null && contains(policy.getPolicyLargeCategory(), category)) {
                reasons.add("찾는 분야(" + category + ") 정책이에요");
            }

            String keyword = intent.getKeyword();
            if (keyword != null
                    && (contains(policy.getPolicyName(), keyword)
                    || contains(policy.getPolicyKeywords(), keyword)
                    || contains(policy.getPolicyMediumCategory(), keyword))) {
                reasons.add("'" + keyword + "' 관련 정책이에요");
            }
        }

        // 2. 나이
        String ageText = ageText(policy);
        if (result.getAgeResult() == MATCH && ageText != null) {
            reasons.add("나이 조건(" + ageText + ")에 맞아요");
        }

        // 3. 지역
        if (result.getRegionResult() == MATCH && hasRegionLimit(policy)) {
            String place = matchedPlaceName(policy, u);
            if (place != null) {
                reasons.add(place + " 거주 조건에 맞아요");
            }
        }

        // 4. 학력
        if (result.getSchoolResult() == MATCH && u.getSchool() != null
                && hasRealCodes(policy.getPolicySchoolCodes(), SchoolRequirement::fromCode,
                SchoolRequirement.NO_RESTRICTION, SchoolRequirement.UNKNOWN)) {
            reasons.add("학력(" + u.getSchool().getDescription() + ") 조건에 맞아요");
        }

        // 5. 취업 상태
        if (result.getJobResult() == MATCH && u.getJob() != null
                && hasRealCodes(policy.getPolicyJobCodes(), JobRequirement::fromCode,
                JobRequirement.NO_RESTRICTION, JobRequirement.UNKNOWN)) {
            reasons.add("취업 상태(" + u.getJob().getDescription() + ") 조건에 맞아요");
        }

        // 6. 혼인
        if (result.getMarriageResult() == MATCH && u.getMarriage() != null
                && hasRealCodes(policy.getPolicyMarriageStatusCodes(), MarriageRequirement::fromCode,
                MarriageRequirement.NO_RESTRICTION, MarriageRequirement.UNKNOWN)) {
            reasons.add("혼인 여부(" + u.getMarriage().getDescription() + ") 조건에 맞아요");
        }

        // 7. 소득
        String incomeText = incomeText(policy);
        if (result.getIncomeResult() == MATCH
                && IncomeConditionType.fromCode(policy.getPolicyIncomeConditionCode())
                == IncomeConditionType.ANNUAL_INCOME
                && incomeText != null) {
            reasons.add("연소득 조건(" + incomeText + ")에 맞아요");
        }

        // 8. 전공
        if (result.getMajorResult() == MATCH && u.getMajor() != null
                && hasRealCodes(policy.getPolicyMajorCodes(), MajorRequirement::fromCode,
                MajorRequirement.NO_RESTRICTION, MajorRequirement.UNKNOWN)) {
            reasons.add("전공(" + u.getMajor().getDescription() + ") 조건에 맞아요");
        }

        // 9. 특화 대상: 정책 대상과 사용자 대상이 겹치는 것만
        if (result.getSpecialTargetResult() == MATCH && u.getSpecialTargets() != null) {
            String matched = matchedSpecialTargets(policy, u.getSpecialTargets());
            if (!matched.isEmpty()) {
                reasons.add("지원 대상(" + matched + ")에 해당해요");
            }
        }

        return reasons;
    }

    // =========================
    // 확인이 필요한 조건
    // =========================
    public List<String> unknownReasons(
            PolicyEvaluationResult result,
            UserPolicyProfileVO user
    ) {

        PolicyVO policy = result.getPolicy();
        UserPolicyProfileVO u = (user != null) ? user : new UserPolicyProfileVO();
        List<String> reasons = new ArrayList<>();

        // 나이
        if (result.getAgeResult() == UNKNOWN) {
            String label = labelWithDetail("나이 조건", ageText(policy));
            reasons.add(u.getAge() == null
                    ? "나이를 알려주시면 " + label + "을 확인할 수 있어요"
                    : label + "은 공고에서 확인이 필요해요");
        }

        if (result.getRegionResult() == UNKNOWN) {
            reasons.add(regionUnknownReason(policy, u));
        }
        addUnknown(reasons, result.getSchoolResult(), u.getSchool() == null, "학력");
        addUnknown(reasons, result.getJobResult(), u.getJob() == null, "취업 상태");
        addUnknown(reasons, result.getMarriageResult(), u.getMarriage() == null, "혼인 여부");

        // 소득
        if (result.getIncomeResult() == UNKNOWN) {

            IncomeConditionType type =
                    IncomeConditionType.fromCode(policy.getPolicyIncomeConditionCode());

            String incomeText = incomeText(policy);

            // 연소득 금액 기준이 있는데 사용자 소득을 모를 때만 "알려주시면" 안내
            // 금액이 비어 있는(0/0) 연소득 조건은 판정 쪽에서 항상 확인 필요이므로 공고 안내
            if (type == IncomeConditionType.ANNUAL_INCOME
                    && incomeText != null
                    && u.getAnnualIncome() == null) {
                reasons.add("연소득을 알려주시면 소득 조건(" + incomeText + ")을 확인할 수 있어요");
            } else {
                String etc = policy.getPolicyIncomeEtcCondition();
                reasons.add(etc == null || etc.isBlank()
                        ? "소득 조건은 공고에서 확인이 필요해요"
                        : "소득 조건은 공고에서 확인이 필요해요: " + shorten(etc));
            }
        }

        addUnknown(reasons, result.getMajorResult(), u.getMajor() == null, "전공");
        addUnknown(reasons, result.getSpecialTargetResult(), u.getSpecialTargets() == null, "지원 대상");

        return reasons;
    }

    // =========================
    // 보조 함수
    // =========================

    // 사용자 정보가 없어서 모르는 경우와, 정책 데이터를 해석하지 못해 모르는 경우를 나눠서 안내
    private void addUnknown(
            List<String> reasons,
            EligibilityStatus status,
            boolean userInfoMissing,
            String what
    ) {

        if (status != UNKNOWN) {
            return;
        }

        reasons.add(userInfoMissing
                ? what + " 정보를 알려주시면 " + what + " 조건을 확인할 수 있어요"
                : what + " 조건은 공고에서 확인이 필요해요");
    }

    // ex) "만 19~34세", "만 19세 이상", "만 34세 이하" / 제한 없음(0 또는 null)이면 null
    private String ageText(PolicyVO policy) {

        Integer min = policy.getPolicyMinAge();
        Integer max = policy.getPolicyMaxAge();

        boolean hasMin = min != null && min > 0;
        boolean hasMax = max != null && max > 0;

        if (hasMin && hasMax) {
            return "만 " + min + "~" + max + "세";
        }
        if (hasMin) {
            return "만 " + min + "세 이상";
        }
        if (hasMax) {
            return "만 " + max + "세 이하";
        }
        return null;
    }

    // ex) "3,000~5,000만원", "5,000만원 이하" (정책 금액은 만원 단위) / 금액 없음이면 null
    private String incomeText(PolicyVO policy) {

        Long min = policy.getPolicyIncomeMinAmount();
        Long max = policy.getPolicyIncomeMaxAmount();

        boolean hasMin = min != null && min > 0;
        boolean hasMax = max != null && max > 0;

        if (hasMin && hasMax) {
            return String.format("%,d~%,d만원", min, max);
        }
        if (hasMax) {
            return String.format("%,d만원 이하", max);
        }
        if (hasMin) {
            return String.format("%,d만원 이상", min);
        }
        return null;
    }

    // "나이 조건" + "만 19~34세" → "나이 조건(만 19~34세)"
    private String labelWithDetail(String label, String detail) {
        return detail == null ? label : label + "(" + detail + ")";
    }

    // =========================
    // 지역 (PolicyEligibilityService.checkRegion과 같은 방식으로 시·도 / 시·군을 구분)
    // =========================

    // 사용자 시·도. 시·도를 모르고 시·군만 알면 시·군 코드에서 복원
    private Region userRegion(UserPolicyProfileVO u) {

        if (u.getRegion() != null) {
            return u.getRegion();
        }
        if (u.getSigunguCode() != null) {
            return Region.fromCode(u.getSigunguCode()).orElse(null);
        }
        return null;
    }

    // 정책이 사용자 시·도 안에서 대상으로 하는 시·군 단위들
    // 시·도 전체 대상이면 null
    private Set<SigunguRegistry.Unit> targetUnitsInSido(PolicyVO policy, Region region) {

        Set<SigunguRegistry.Unit> units = new LinkedHashSet<>();

        for (String code : policy.getPolicyRegionCodes().split(",")) {

            String trimmed = code.trim();
            Optional<Region> codeRegion = Region.fromCode(trimmed);

            if (trimmed.isEmpty() || codeRegion.isEmpty() || codeRegion.get() != region) {
                continue;
            }

            Optional<SigunguRegistry.Unit> unit = SigunguRegistry.fromCode(trimmed);
            if (unit.isEmpty()) {
                return null;    // 41000처럼 시·도 전체 코드
            }
            units.add(unit.get());
        }

        String sidoCode = region.getRegionCodes().get(0);
        long distinctUnits = units.stream().map(SigunguRegistry.Unit::unitCode).distinct().count();

        if (distinctUnits >= SigunguRegistry.unitCount(sidoCode)) {
            return null;        // 그 시·도의 모든 시·군 포함 → 시·도 전체 대상
        }
        return units;
    }

    // 지역 MATCH일 때 이유에 쓸 이름: 시·도 전체 대상이면 "경기도", 특정 시·군 대상이면 "안양시"
    private String matchedPlaceName(PolicyVO policy, UserPolicyProfileVO u) {

        Region region = userRegion(u);
        if (region == null) {
            return null;
        }

        Set<SigunguRegistry.Unit> units = targetUnitsInSido(policy, region);
        if (units == null) {
            return region.getRegionName();
        }

        return SigunguRegistry.fromUnitCode(u.getSigunguCode())
                .map(SigunguRegistry.Unit::unitName)
                .orElse(region.getRegionName());
    }

    // 지역 UNKNOWN 안내
    private String regionUnknownReason(PolicyVO policy, UserPolicyProfileVO u) {

        Region region = userRegion(u);

        // 사는 곳을 전혀 모름
        if (region == null) {
            return "사는 지역을 알려주시면 지역 조건을 확인할 수 있어요";
        }

        // 시·도는 아는데, 정책이 그 시·도의 일부 시·군만 대상 → 시·군을 알아야 함
        if (u.getSigunguCode() == null) {
            Set<SigunguRegistry.Unit> units = targetUnitsInSido(policy, region);
            if (units != null && !units.isEmpty()) {
                return "이 정책은 " + unitNames(units) + " 대상이에요. "
                        + "사는 시·군을 알려주시면 확인할 수 있어요";
            }
        }

        // 정책 지역 코드를 해석하지 못함
        return "지역 조건은 공고에서 확인이 필요해요";
    }

    // "군포시, 의왕시 외 2곳"
    private String unitNames(Set<SigunguRegistry.Unit> units) {

        List<String> names = units.stream()
                .map(SigunguRegistry.Unit::unitName)
                .distinct()
                .toList();

        if (names.size() <= 2) {
            return String.join(", ", names);
        }
        return names.get(0) + ", " + names.get(1) + " 외 " + (names.size() - 2) + "곳";
    }

    private boolean hasRegionLimit(PolicyVO policy) {

        String codes = policy.getPolicyRegionCodes();

        if (codes == null || codes.isBlank()) {
            return false;
        }

        return codes.split(",").length <= NATIONWIDE_REGION_CODE_COUNT;
    }

    // 정책 코드 중에 "제한없음"·"알 수 없음"이 아닌 실제 조건이 하나라도 있는지
    private <E extends Enum<E>> boolean hasRealCodes(
            String codes,
            Function<String, E> fromCode,
            E noRestriction,
            E unknown
    ) {

        if (codes == null || codes.isBlank()) {
            return false;
        }

        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .map(fromCode)
                .anyMatch(value -> value != null
                        && value != noRestriction
                        && value != unknown);
    }

    private String matchedSpecialTargets(
            PolicyVO policy,
            Set<SpecialTargetRequirement> userTargets
    ) {

        String codes = policy.getPolicySpecialTargetCodes();

        if (codes == null || codes.isBlank()) {
            return "";
        }

        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .map(SpecialTargetRequirement::fromCode)
                .filter(Objects::nonNull)
                .filter(userTargets::contains)
                .distinct()
                .map(SpecialTargetRequirement::getDescription)
                .collect(Collectors.joining(", "));
    }

    private boolean contains(String text, String word) {
        return text != null && word != null && text.contains(word);
    }

    private String shorten(String text) {

        String oneLine = text.replaceAll("\\s+", " ").trim();

        return oneLine.length() <= ETC_TEXT_LIMIT
                ? oneLine
                : oneLine.substring(0, ETC_TEXT_LIMIT) + "…";
    }
}
