package com.app.YouthCompass.service.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;

public interface MemberService {
    public void join(MemberJoinRequestDTO member);
}
