package com.app.YouthCompass.profile.dto;

import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.profile.domain.MemberProfileVO;
import lombok.Data;
import lombok.NoArgsConstructor;

// 마이페이지 "내 조건 프로필" 입력 폼
// 빈 칸("")은 Spring이 enum·Long을 null로 바꿔 줌 → "모름"으로 저장
@Data
@NoArgsConstructor
public class MemberProfileFormDTO {

    private String sigunguCode;
    private SchoolRequirement school;
    private JobRequirement job;
    private MarriageRequirement marriage;
    private MajorRequirement major;
    private Long annualIncome;

    // DB 값 → 폼 (수정 화면에 기존 값 채우기)
    public static MemberProfileFormDTO from(MemberProfileVO vo) {

        MemberProfileFormDTO form = new MemberProfileFormDTO();
        form.setSigunguCode(vo.getSigunguCode());
        form.setSchool(vo.getSchool());
        form.setJob(vo.getJob());
        form.setMarriage(vo.getMarriage());
        form.setMajor(vo.getMajor());
        form.setAnnualIncome(vo.getAnnualIncome());
        return form;
    }

    // 폼 → 저장용 VO
    // 프로필 칸 수 / 채운 칸 수 (마이페이지 진행 표시용) — 칸을 늘리면 TOTAL_FIELDS도 같이
    public static final int TOTAL_FIELDS = 6;

    public int filledCount() {
        int count = 0;
        if (sigunguCode != null && !sigunguCode.isBlank()) count++;
        if (school != null) count++;
        if (job != null) count++;
        if (marriage != null) count++;
        if (major != null) count++;
        if (annualIncome != null) count++;
        return count;
    }

    public MemberProfileVO toVO(Long memberId) {

        return MemberProfileVO.builder()
                .memberId(memberId)
                .sigunguCode(validSigungu(sigunguCode))
                .school(school)
                .job(job)
                .marriage(marriage)
                .major(major)
                .annualIncome(annualIncome)
                .build();
    }

    // 목록에 없는 코드(조작된 값)는 저장하지 않음
    private static String validSigungu(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }
        return SigunguRegistry.fromUnitCode(code)
                .map(SigunguRegistry.Unit::unitCode)
                .orElse(null);
    }
}