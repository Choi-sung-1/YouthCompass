package com.app.YouthCompass.profile;

import com.app.YouthCompass.profile.domain.MemberProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberProfileMapper memberProfileMapper;

    // 회원 조건 프로필 조회 (회원 ID가 없거나 저장 전이면 empty)
    public Optional<MemberProfileVO> findProfile(Long memberId) {

        if (memberId == null) {
            return Optional.empty();
        }
        return memberProfileMapper.selectByMemberId(memberId);
    }

    // 프로필 저장 (전체 덮어쓰기)
    public void saveProfile(MemberProfileVO profile) {
        memberProfileMapper.upsertProfile(profile);
    }
}