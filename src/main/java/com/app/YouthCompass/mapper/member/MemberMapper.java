package com.app.YouthCompass.mapper.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;
import com.app.YouthCompass.domain.vo.member.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;
@Mapper
public interface MemberMapper {

    public void insertMember(MemberJoinRequestDTO member);
    public Optional<MemberVO> selectMemberByMemberLoginId(String memberLoginId);
}
