package com.app.YouthCompass.policy.recommendation;

import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.profile.MemberProfileService;
import com.app.YouthCompass.profile.domain.MemberProfileVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// 회원정보로 채운 항목에 출처를 붙여서 돌려주는지
// (마이페이지에 지역을 저장 안 했는데 "회원정보에서 가져왔어요"라고만 나와 어디서 온 값인지 헷갈리던 문제)
class MemberProfileDefaultsTest {

    private final MemberProfileService profileService = mock(MemberProfileService.class);
    private final MemberProfileDefaults defaults = new MemberProfileDefaults(profileService);

    private MemberVO member() {
        MemberVO member = new MemberVO();
        member.setMemberId(1L);
        member.setMemberRegion("41");   // 가입 때 고른 지역: 경기도
        member.setMemberBirth(LocalDate.now().minusYears(25));
        return member;
    }

    @Test
    void 마이페이지에_시군이_없으면_지역은_가입_정보에서() {

        when(profileService.findProfile(any())).thenReturn(Optional.empty());
        UserPolicyProfileVO profile = new UserPolicyProfileVO();

        assertThat(defaults.fill(profile, member())).containsExactly("나이(생년월일)", "지역(가입 정보)");
        assertThat(profile.getRegion()).isEqualTo(Region.GYEONGGI);
    }

    @Test
    void 마이페이지에_저장한_값은_마이페이지에서() {

        when(profileService.findProfile(any())).thenReturn(Optional.of(MemberProfileVO.builder()
                .sigunguCode("41220")   // 평택시
                .school(SchoolRequirement.UNIVERSITY_GRADUATE)
                .build()));
        UserPolicyProfileVO profile = new UserPolicyProfileVO();

        assertThat(defaults.fill(profile, member()))
                .containsExactly("나이(생년월일)", "지역(마이페이지)", "학력(마이페이지)");
        assertThat(profile.getSigunguCode()).isEqualTo("41220");
    }
}
