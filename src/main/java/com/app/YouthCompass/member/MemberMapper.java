package com.app.YouthCompass.member;

import com.app.YouthCompass.member.dto.MemberJoinRequestDTO;
import com.app.YouthCompass.member.domain.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;
@Mapper
public interface MemberMapper {

    public void insertMember(MemberJoinRequestDTO member);
    public Optional<MemberVO> selectMemberByMemberLoginId(String memberLoginId);
}
