package com.app.YouthCompass.domain.dto.member;

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
}
