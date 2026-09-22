package com.app.YouthCompass.service.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;
import com.app.YouthCompass.repository.member.MemberDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberDAO memberDAO;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void join(MemberJoinRequestDTO member) {
//        비밀번호 암호화
        member.setMemberPassword(passwordEncoder.encode(member.getMemberPassword()));
        member.setMemberProvider("local");
        memberDAO.saveMember(member);
    }
}
