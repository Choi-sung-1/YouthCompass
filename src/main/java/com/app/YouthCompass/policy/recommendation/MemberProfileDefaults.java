package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.profile.MemberProfileService;
import com.app.YouthCompass.profile.domain.MemberProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// ============================================================
// 로그인 회원 정보로 추천 조건의 빈칸을 채운다
//  - 문장에서 AI가 찾지 못한 값만 채움 (사용자가 문장에 쓴 값이 항상 우선)
//  - 생년월일 → 만 나이
//  - 마이페이지 조건 프로필 → 시·군, 학력, 취업, 혼인, 전공, 연소득
//  - 프로필에 시·군이 없으면 가입 지역 → 시·도
//  - 채운 항목 이름을 돌려줘서 화면에 "회원정보에서 가져왔어요" 안내
//  - 시그니처는 팀 약속(정책 상세에서도 호출)이라 바꾸지 않음
// ============================================================
@Component
@RequiredArgsConstructor
public class MemberProfileDefaults {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private final MemberProfileService memberProfileService;

    public List<String> fill(UserPolicyProfileVO profile, MemberVO member) {

        List<String> filled = new ArrayList<>();

        if (profile == null || member == null) {
            return filled;
        }

        // 나이: 생년월일로 만 나이 계산 (미래 날짜 등 이상한 값은 무시)
        if (profile.getAge() == null && member.getMemberBirth() != null) {
            int age = Period.between(member.getMemberBirth(), LocalDate.now(KOREA)).getYears();
            if (age > 0 && age <= 120) {
                profile.setAge(age);
                filled.add("나이");
            }
        }

        // 마이페이지 조건 프로필 (저장 전이면 null)
        MemberProfileVO saved = memberProfileService.findProfile(member.getMemberId()).orElse(null);

        fillRegion(profile, member, saved, filled);

        if (saved == null) {
            return filled;
        }

        // 학력·취업·혼인·전공: 문장에서 못 찾은 것만 (UNKNOWN도 못 찾은 것으로 봄)
        if (isBlank(profile.getSchool()) && saved.getSchool() != null) {
            profile.setSchool(saved.getSchool());
            filled.add("학력");
        }
        if (isBlank(profile.getJob()) && saved.getJob() != null) {
            profile.setJob(saved.getJob());
            filled.add("취업 상태");
        }
        if (isBlank(profile.getMarriage()) && saved.getMarriage() != null) {
            profile.setMarriage(saved.getMarriage());
            filled.add("혼인 상태");
        }
        if (isBlank(profile.getMajor()) && saved.getMajor() != null) {
            profile.setMajor(saved.getMajor());
            filled.add("전공");
        }

        // 연소득 (만원)
        if (profile.getAnnualIncome() == null && saved.getAnnualIncome() != null) {
            profile.setAnnualIncome(saved.getAnnualIncome());
            filled.add("연소득");
        }

        return filled;
    }

    // 지역·시·군 채우기
    //  1) 문장에 지역도 시·군도 없음 → 프로필 시·군(+ 그 시·도), 없으면 가입 지역 시·도
    //  2) 문장에 시·도만 있음 → 프로필 시·군이 같은 시·도일 때만 시·군 채움
    private void fillRegion(UserPolicyProfileVO profile, MemberVO member,
                            MemberProfileVO saved, List<String> filled) {

        Optional<SigunguRegistry.Unit> savedUnit = Optional.ofNullable(saved)
                .flatMap(s -> SigunguRegistry.fromUnitCode(s.getSigunguCode()));

        // 문장에서 시·군을 이미 찾았으면 손대지 않음
        if (profile.getSigunguCode() != null) {
            return;
        }

        if (profile.getRegion() == null) {

            if (savedUnit.isPresent()) {
                SigunguRegistry.Unit unit = savedUnit.get();
                profile.setSigunguCode(unit.unitCode());
                Region.fromCode(unit.sidoCode()).ifPresent(profile::setRegion);
                filled.add("지역");
                return;
            }

            toRegion(member.getMemberRegion()).ifPresent(region -> {
                profile.setRegion(region);
                filled.add("지역");
            });
            return;
        }

        // 문장의 시·도와 프로필 시·군의 시·도가 같을 때만 (다른 지역을 말했으면 그 말이 우선)
        savedUnit
                .filter(unit -> Region.fromCode(unit.sidoCode())
                        .map(region -> region == profile.getRegion())
                        .orElse(false))
                .ifPresent(unit -> {
                    profile.setSigunguCode(unit.unitCode());
                    filled.add("시·군");
                });
    }

    // AI가 못 찾은 값: null 또는 UNKNOWN
    private boolean isBlank(Enum<?> value) {
        return value == null || "UNKNOWN".equals(value.name());
    }

    // 가입 폼은 시·도 코드("41")를 저장하지만, 예전 데이터는 "경기도"처럼 이름일 수 있어서 둘 다 처리
    private Optional<Region> toRegion(String memberRegion) {

        if (memberRegion == null || memberRegion.isBlank()) {
            return Optional.empty();
        }

        String value = memberRegion.trim();

        Optional<Region> byCode = Region.fromCode(value);
        if (byCode.isPresent()) {
            return byCode;
        }

        return Arrays.stream(Region.values())
                .filter(r -> r.getRegionName().equals(value) || r.getRegionName().startsWith(value))
                .findFirst();
    }
}