package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.policy.domain.eligibility.SpecialTargetRequirement;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicyCategories;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// 12번: 사용자가 직접 확인·수정한 조건
// AI가 뽑은 조건(UserPolicyProfileVO + PolicySearchIntent)을 화면 입력칸에 채워 보여주고,
// 사용자가 고쳐서 다시 보내면 AI 없이 이 값으로 바로 추천한다.
// 빈 칸 = 모름(null)
@Data
public class ConditionRequestDTO {

    // 처음 입력한 문장 (다시 추천해도 입력창에 그대로 남기기 위함)
    private String sentence;

    private Integer age;

    private Region region;

    // 시·군 이름 (예: 안양시). 저장은 코드로 하지만 사용자는 이름으로 보고 고친다
    private String sigungu;

    private SchoolRequirement school;

    private JobRequirement job;

    private MarriageRequirement marriage;

    // 개인 연소득 (만원)
    private Long annualIncome;

    private MajorRequirement major;

    // 아무것도 고르지 않으면 null(모름)
    private Set<SpecialTargetRequirement> specialTargets;

    private String category;

    private String keyword;

    // =========================
    // AI가 뽑은 조건 → 입력칸
    // =========================
    public static ConditionRequestDTO from(
            UserPolicyProfileVO profile,
            PolicySearchIntent intent,
            String sentence
    ) {

        ConditionRequestDTO dto = new ConditionRequestDTO();
        dto.setSentence(sentence);

        if (profile != null) {
            dto.setAge(profile.getAge());
            dto.setRegion(profile.getRegion());
            dto.setSigungu(
                    SigunguRegistry.fromUnitCode(profile.getSigunguCode())
                            .map(SigunguRegistry.Unit::unitName)
                            .orElse(null)
            );
            dto.setSchool(profile.getSchool());
            dto.setJob(profile.getJob());
            dto.setMarriage(profile.getMarriage());
            dto.setAnnualIncome(profile.getAnnualIncome());
            dto.setMajor(profile.getMajor());
            dto.setSpecialTargets(
                    profile.getSpecialTargets() == null ? null : new HashSet<>(profile.getSpecialTargets())
            );
        }

        if (intent != null) {
            dto.setCategory(PolicyCategories.normalize(intent.getCategory()));
            dto.setKeyword(intent.getKeyword());
        }

        return dto;
    }

    // =========================
    // 입력칸 → 추천에 쓰는 조건
    // =========================
    public UserPolicyProfileVO toProfile() {

        Optional<SigunguRegistry.Unit> unit = findSigungu();

        // 시·도를 비웠는데 시·군으로 찾았으면 시·도도 채움 (AI 추출과 같은 규칙)
        Region resolvedRegion = region;
        if (resolvedRegion == null && unit.isPresent()) {
            resolvedRegion = Region.fromCode(unit.get().sidoCode()).orElse(null);
        }

        return UserPolicyProfileVO.builder()
                .age(age)
                .region(resolvedRegion)
                .sigunguCode(unit.map(SigunguRegistry.Unit::unitCode).orElse(null))
                .school(school)
                .job(job)
                .marriage(marriage)
                .annualIncome(annualIncome)
                .major(major)
                .specialTargets(specialTargets == null || specialTargets.isEmpty() ? null : specialTargets)
                .build();
    }

    public PolicySearchIntent toIntent() {

        return PolicySearchIntent.builder()
                .category(PolicyCategories.normalize(category))
                .keyword(blankToNull(keyword))
                .build();
    }

    // 시·군 이름을 적었는데 코드표에서 찾지 못했는지 (화면 안내용)
    public boolean isSigunguNotFound() {
        return blankToNull(sigungu) != null && findSigungu().isEmpty();
    }

    // 시·도를 알면 그 시·도 안에서만 찾음 (AI 추출과 같은 규칙)
    private Optional<SigunguRegistry.Unit> findSigungu() {

        String name = blankToNull(sigungu);
        if (name == null) {
            return Optional.empty();
        }

        String sidoCode = (region != null) ? region.getRegionCodes().get(0) : null;
        return SigunguRegistry.findByName(sidoCode, name);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
