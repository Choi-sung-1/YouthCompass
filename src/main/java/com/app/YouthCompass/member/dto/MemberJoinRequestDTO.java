package com.app.YouthCompass.member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberJoinRequestDTO {
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String memberLoginId;
    private String memberPassword;
    private String memberRegion;
    private LocalDate memberBirth;
    private String memberGender;
    private String memberPhone;
    private String memberProvider;

    // 거주 시·군 단위코드 (선택). TBL_MEMBER가 아니라 TBL_MEMBER_PROFILE에 저장
    private String sigunguCode;
}
