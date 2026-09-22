package com.app.YouthCompass.member;

import com.app.YouthCompass.domain.dto.member.MemberJoinRequestDTO;
import com.app.YouthCompass.service.member.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class MemberTest {
    @Autowired
    private  MemberService memberService;

    public final MemberJoinRequestDTO memberJoinRequestDTO = new MemberJoinRequestDTO();

    @Test
    public void joiTest(){
        memberJoinRequestDTO.setMemberName("김성원");
        memberJoinRequestDTO.setMemberEmail("span@naver.com");
        memberJoinRequestDTO.setMemberLoginId("test2");
        memberJoinRequestDTO.setMemberPassword("123456");
        memberJoinRequestDTO.setMemberRegion("경기도");
        memberJoinRequestDTO.setMemberBirth(LocalDate.now());
        memberJoinRequestDTO.setMemberGender("남성");
        memberJoinRequestDTO.setMemberPhone("010-1234-1234");
        memberJoinRequestDTO.setMemberProvider("local");
        memberService.join(memberJoinRequestDTO);
    }
}
