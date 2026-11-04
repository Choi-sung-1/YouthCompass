package com.app.YouthCompass.policy.eligibility;

import com.app.YouthCompass.policy.PolicyMapper;
import com.app.YouthCompass.policy.domain.eligibility.IncomeConditionType;
import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SpecialTargetRequirement;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.ConditionRequestDTO;
import com.app.YouthCompass.policy.dto.MissedPolicyDTO;
import com.app.YouthCompass.policy.dto.RecheckQuestionDTO;
import com.app.YouthCompass.policy.dto.RecheckQuestionDTO.Option;
import com.app.YouthCompass.policy.dto.RecheckRequestDTO;
import com.app.YouthCompass.policy.dto.RecheckResponseDTO;
import com.app.YouthCompass.policy.recommendation.RecommendReasonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus.UNKNOWN;

// ============================================================
// 추천 카드 한 장 다시 판정 (추천 카드 강화 3번)
//  - 조건 수정 폼의 값 + 사용자가 답한 값으로 그 정책 하나만 다시 판정
//  - 사용자 정보가 없어서 생긴 확인 필요 조건만 질문으로 돌려줌
//    (정책 문장 조건처럼 답해도 못 푸는 건 질문하지 않고 "공고 확인" 안내 그대로)
//  - AI 호출 없음, DB 저장 없음
// ============================================================
@Service
@RequiredArgsConstructor
public class RecheckService {

    public static final String ELIGIBLE = "ELIGIBLE";
    public static final String NEED_CHECK = "NEED_CHECK";
    public static final String NOT_ELIGIBLE = "NOT_ELIGIBLE";

    private final PolicyMapper policyMapper;
    private final PolicyEligibilityService eligibilityService;
    private final MissedPolicyFinder missedPolicyFinder;   // 안 맞는 조건 안내 문구 재사용
    private final RecommendReasonBuilder reasonBuilder;    // B 담당 — 호출만 (추천 카드와 같은 문장)

    // 없는 정책이면 empty
    public Optional<RecheckResponseDTO> recheck(RecheckRequestDTO request) {

        if (request == null || request.getPolicyId() == null) {
            return Optional.empty();
        }

        PolicyVO policy = policyMapper.findPolicyById(request.getPolicyId());
        if (policy == null) {
            return Optional.empty();
        }

        UserPolicyProfileVO profile = toProfile(request);
        PolicyEvaluationResult result = eligibilityService.evaluate(profile, policy);

        String status;
        String notMatchCondition = null;
        String notMatchHint = null;

        if (result.getNotMatchCount() > 0) {
            status = NOT_ELIGIBLE;
            Optional<MissedPolicyDTO> described = missedPolicyFinder.describeNotMatch(result, profile);
            notMatchCondition = described.map(MissedPolicyDTO::getMissedCondition).orElse(null);
            notMatchHint = described.map(MissedPolicyDTO::getHint).orElse(null);
        } else if (result.getUnknownCount() > 0) {
            status = NEED_CHECK;
        } else {
            status = ELIGIBLE;
        }

        return Optional.of(new RecheckResponseDTO(
                status,
                reasonBuilder.reasons(result, profile, null),
                reasonBuilder.unknownReasons(result, profile),
                notMatchCondition,
                notMatchHint,
                NOT_ELIGIBLE.equals(status) ? List.of() : questions(result, profile)
        ));
    }

    // 조건 수정 폼과 같은 규칙으로 프로필 만들기 (시·군 이름 → 코드 포함)
    private UserPolicyProfileVO toProfile(RecheckRequestDTO request) {

        ConditionRequestDTO conditions = request.getConditions() != null
                ? request.getConditions()
                : new ConditionRequestDTO();

        UserPolicyProfileVO profile = conditions.toProfile();

        // "해당 없음" = 빈 목록 (null은 모름) → 지원 대상 제한이 있는 정책은 안 맞음으로 판정됨
        if (request.isNoSpecialTarget() && profile.getSpecialTargets() == null) {
            profile.setSpecialTargets(Set.of());
        }
        return profile;
    }

    // =========================
    // 답하면 풀리는 확인 필요 조건 → 질문
    // =========================
    private List<RecheckQuestionDTO> questions(PolicyEvaluationResult r, UserPolicyProfileVO u) {

        PolicyVO p = r.getPolicy();
        List<RecheckQuestionDTO> questions = new ArrayList<>();

        if (r.getAgeResult() == UNKNOWN && u.getAge() == null) {
            questions.add(RecheckQuestionDTO.number("age", "나이 (만)"));
        }

        // 지역: 시·도부터, 시·도를 알면 시·군
        if (r.getRegionResult() == UNKNOWN) {
            if (regionOf(u) == null) {
                questions.add(RecheckQuestionDTO.select("region", "사는 시·도", regionOptions()));
            } else if (u.getSigunguCode() == null) {
                questions.add(RecheckQuestionDTO.text("sigungu", "사는 시·군 (예: 평택시)"));
            }
        }

        if (r.getSchoolResult() == UNKNOWN && isBlank(u.getSchool())) {
            questions.add(RecheckQuestionDTO.select("school", "학력",
                    options(SchoolRequirement.values(), SchoolRequirement::getDescription)));
        }
        if (r.getJobResult() == UNKNOWN && isBlank(u.getJob())) {
            questions.add(RecheckQuestionDTO.select("job", "취업 상태",
                    options(JobRequirement.values(), JobRequirement::getDescription)));
        }
        if (r.getMarriageResult() == UNKNOWN && isBlank(u.getMarriage())) {
            questions.add(RecheckQuestionDTO.select("marriage", "혼인 여부",
                    options(MarriageRequirement.values(), MarriageRequirement::getDescription)));
        }

        // 소득: 연소득 금액 기준이 있을 때만 (기타 문장 조건은 답해도 판정 불가)
        if (r.getIncomeResult() == UNKNOWN && u.getAnnualIncome() == null && hasAnnualIncomeAmount(p)) {
            questions.add(RecheckQuestionDTO.number("annualIncome", "연소득 (만원)"));
        }

        if (r.getMajorResult() == UNKNOWN && isBlank(u.getMajor())) {
            questions.add(RecheckQuestionDTO.select("major", "전공",
                    options(MajorRequirement.values(), MajorRequirement::getDescription)));
        }

        // 지원 대상: 이 정책이 요구하는 대상만 보여줌 (화면에서 "해당 없음" 선택지가 추가됨)
        if (r.getSpecialTargetResult() == UNKNOWN && u.getSpecialTargets() == null) {
            List<Option> targets = policyTargetOptions(p);
            if (!targets.isEmpty()) {
                questions.add(RecheckQuestionDTO.checkbox("specialTargets", "해당하는 지원 대상", targets));
            }
        }

        return questions;
    }

    private Region regionOf(UserPolicyProfileVO u) {
        if (u.getRegion() != null) {
            return u.getRegion();
        }
        return u.getSigunguCode() == null ? null : Region.fromCode(u.getSigunguCode()).orElse(null);
    }

    private List<Option> regionOptions() {
        return Arrays.stream(Region.values())
                .map(region -> new Option(region.name(), region.getRegionName()))
                .toList();
    }

    // 사용자가 고를 수 있는 값만 (알 수 없음·제한없음 제외 — 조건 수정 폼과 같은 기준)
    private <E extends Enum<E>> List<Option> options(E[] values, Function<E, String> label) {
        return Arrays.stream(values)
                .filter(v -> !v.name().equals("UNKNOWN") && !v.name().equals("NO_RESTRICTION"))
                .map(v -> new Option(v.name(), label.apply(v)))
                .toList();
    }

    private List<Option> policyTargetOptions(PolicyVO p) {

        String codes = p.getPolicySpecialTargetCodes();
        if (codes == null || codes.isBlank()) {
            return List.of();
        }

        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(SpecialTargetRequirement::fromCode)
                .filter(t -> t != SpecialTargetRequirement.NO_RESTRICTION && t != SpecialTargetRequirement.UNKNOWN)
                .distinct()
                .map(t -> new Option(t.name(), t.getDescription()))
                .toList();
    }

    private boolean hasAnnualIncomeAmount(PolicyVO p) {

        if (IncomeConditionType.fromCode(p.getPolicyIncomeConditionCode()) != IncomeConditionType.ANNUAL_INCOME) {
            return false;
        }
        Long min = p.getPolicyIncomeMinAmount();
        Long max = p.getPolicyIncomeMaxAmount();
        return (min != null && min > 0) || (max != null && max > 0);
    }

    // AI·폼에서 못 정한 값: null 또는 UNKNOWN
    private boolean isBlank(Enum<?> value) {
        return value == null || "UNKNOWN".equals(value.name());
    }
}