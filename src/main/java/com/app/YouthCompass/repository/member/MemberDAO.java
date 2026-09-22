package com.app.YouthCompass.repository.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;
import com.app.YouthCompass.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberDAO {
    private final MemberMapper memberMapper;

    public void saveMember(MemberJoinRequestDTO member) {
        memberMapper.insertMember(member);
    }
}
