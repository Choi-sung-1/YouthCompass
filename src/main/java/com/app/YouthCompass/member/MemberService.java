package com.app.YouthCompass.member;

import com.app.YouthCompass.member.dto.MemberJoinRequestDTO;
import com.app.YouthCompass.member.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public void join(MemberJoinRequestDTO member) {

        member.setMemberPassword(
                passwordEncoder.encode(member.getMemberPassword())
        );

        memberMapper.insertMember(member);
    }

    public Optional<MemberVO> findMember(String memberLoginId) {
        return memberMapper.selectMemberByMemberLoginId(memberLoginId);
    }
}