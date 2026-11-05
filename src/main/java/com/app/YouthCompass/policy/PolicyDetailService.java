package com.app.YouthCompass.policy;

import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.PolicyEvaluationResult;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.PolicyDetailResponseDTO;
import com.app.YouthCompass.policy.dto.PolicyDetailResponseDTO.ConditionRow;
import com.app.YouthCompass.policy.dto.PolicyListItemDTO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import com.app.YouthCompass.policy.recommendation.MemberProfileDefaults;
import com.app.YouthCompass.policy.recommendation.RecommendReasonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// 정책 상세 + 로그인 회원의 조건별 판정
// (판정·문장 로직은 추천과 같은 것을 재사용 → 추천 결과와 상세 화면의 판정이 항상 일치)
@Service
@RequiredArgsConstructor
public class PolicyDetailService {

    private final PolicyMapper policyMapper;
    private final PolicyEligibilityService eligibilityService;
    private final MemberProfileDefaults memberProfileDefaults;   // B 담당 — 호출만
    private final RecommendReasonBuilder reasonBuilder;

    // member: 로그인 회원 (비로그인이면 null → 판정 없이 정책 정보만)
    public Optional<PolicyDetailResponseDTO> getDetail(Long policyId, MemberVO member) {

        PolicyVO policy = policyMapper.findPolicyById(policyId);
        if (policy == null) {
            return Optional.empty();
        }

        PolicyDetailResponseDTO dto = new PolicyDetailResponseDTO();
        dto.setPolicy(policy);
        dto.setCard(PolicyListItemDTO.from(policy));
        dto.setAgeText(ageText(policy));
        dto.setBusinessPeriodText(businessPeriodText(policy));
        dto.setInactive("INACTIVE".equals(policy.getPolicyStatus()));

        if (member != null) {
            evaluate(dto, policy, member);
        }
        return Optional.of(dto);
    }

    private void evaluate(PolicyDetailResponseDTO dto, PolicyVO policy, MemberVO member) {

        // 회원정보로 채울 수 있는 만큼 채운 프로필 (B가 fill()을 확장하면 자동으로 더 정확해짐)
        UserPolicyProfileVO profile = new UserPolicyProfileVO();
        dto.setFilledFromMember(memberProfileDefaults.fill(profile, member));

        PolicyEvaluationResult result = eligibilityService.evaluate(profile, policy);

        dto.setEvaluated(true);
        dto.setConditions(List.of(
                new ConditionRow("나이", result.getAgeResult()),
                new ConditionRow("지역", result.getRegionResult()),
                new ConditionRow("학력", result.getSchoolResult()),
                new ConditionRow("취업 상태", result.getJobResult()),
                new ConditionRow("혼인 여부", result.getMarriageResult()),
                new ConditionRow("소득", result.getIncomeResult()),
                new ConditionRow("전공", result.getMajorResult()),
                new ConditionRow("지원 대상", result.getSpecialTargetResult())
        ));
        dto.setMatchedReasons(reasonBuilder.reasons(result, profile, null));
        dto.setUnknownReasons(reasonBuilder.unknownReasons(result, profile));
        dto.setNotMatchCount(result.getNotMatchCount());
        dto.setUnknownCount(result.getUnknownCount());
    }

    // 나이 0 또는 null = 제한 없음 (판정 쪽과 같은 기준)
    private String ageText(PolicyVO p) {

        Integer min = positive(p.getPolicyMinAge());
        Integer max = positive(p.getPolicyMaxAge());

        if (min == null && max == null) return "제한 없음";
        if (min == null) return "만 " + max + "세 이하";
        if (max == null) return "만 " + min + "세 이상";
        return "만 " + min + "세 ~ " + max + "세";
    }

    // 사업기간: 날짜가 있으면 날짜, 없으면 기타 문구("연중 계속" 등), 둘 다 없으면 null (화면에서 숨김)
    private String businessPeriodText(PolicyVO p) {

        LocalDate start = p.getPolicyBusinessStartDate();
        LocalDate end = p.getPolicyBusinessEndDate();

        if (start != null || end != null) {
            return (start == null ? "" : start.format(DOT_DATE)) + " ~ " + (end == null ? "" : end.format(DOT_DATE));
        }
        return p.getPolicyBusinessPeriodEtc();
    }

    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private Integer positive(Integer value) {
        return value != null && value > 0 ? value : null;
    }
}