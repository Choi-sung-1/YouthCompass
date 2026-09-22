package com.app.YouthCompass.mapper.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    public void insertMember(MemberJoinRequestDTO member);
}
