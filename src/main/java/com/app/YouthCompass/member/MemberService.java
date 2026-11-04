package com.app.YouthCompass.member;

import com.app.YouthCompass.member.dto.MemberJoinRequestDTO;
import com.app.YouthCompass.member.domain.MemberVO;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.profile.MemberProfileService;
import com.app.YouthCompass.profile.domain.MemberProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final MemberProfileService memberProfileService;

    // 회원 저장 + (고른 경우) 시·군을 조건 프로필에 저장 → 둘 중 하나라도 실패하면 둘 다 취소
    @Transactional
    public void join(MemberJoinRequestDTO member) {

        member.setMemberPassword(
                passwordEncoder.encode(member.getMemberPassword())
        );
        // 일반 회원가입은 항상 LOCAL (카카오 로그인은 별도 흐름에서 세팅)
        member.setMemberProvider("LOCAL");

        // useGeneratedKeys → 저장 후 member.memberId 가 채워짐
        memberMapper.insertMember(member);

        // TBL_MEMBER에는 시·군 칸이 없어서 조건 프로필에 저장 (마이페이지·추천 자동 채움에서 사용)
        validSigungu(member.getMemberRegion(), member.getSigunguCode())
                .ifPresent(code -> memberProfileService.saveProfile(
                        MemberProfileVO.builder()
                                .memberId(member.getMemberId())
                                .sigunguCode(code)
                                .build()));
    }

    public Optional<MemberVO> findMember(String memberLoginId) {
        return memberMapper.selectMemberByMemberLoginId(memberLoginId);
    }

    // 고른 시·군이 실제로 있고, 고른 시·도 소속일 때만 저장 (조작된 값·불일치는 무시)
    private Optional<String> validSigungu(String regionCode, String sigunguCode) {

        if (sigunguCode == null || sigunguCode.isBlank()) {
            return Optional.empty();
        }

        Optional<Region> region = Region.fromCode(regionCode);
        if (region.isEmpty()) {
            return Optional.empty();
        }

        return SigunguRegistry.fromUnitCode(sigunguCode)
                .filter(unit -> Region.fromCode(unit.sidoCode()).equals(region))
                .map(SigunguRegistry.Unit::unitCode);
    }
}