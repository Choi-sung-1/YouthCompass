package com.app.YouthCompass.profile;

import com.app.YouthCompass.profile.domain.MemberProfileVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MemberProfileMapper {

    // 회원의 조건 프로필 (없으면 empty)
    Optional<MemberProfileVO> selectByMemberId(Long memberId);

    // 있으면 수정, 없으면 추가
    void upsertProfile(MemberProfileVO profile);
}