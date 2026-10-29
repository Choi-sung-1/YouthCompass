package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
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
//  - 생년월일 → 만 나이, 가입 지역 → 시·도
//  - 채운 항목 이름을 돌려줘서 화면에 "회원정보에서 가져왔어요" 안내
// ============================================================
@Component
public class MemberProfileDefaults {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

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

        // 지역: 시·도도 시·군도 모를 때만 (시·군만 알면 판정 쪽에서 시·도를 복원함)
        if (profile.getRegion() == null && profile.getSigunguCode() == null) {
            toRegion(member.getMemberRegion()).ifPresent(region -> {
                profile.setRegion(region);
                filled.add("지역");
            });
        }

        return filled;
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
