package com.app.YouthCompass.domain.vo.member;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberVO {
//    pk
    private Long memberId;
//    회원 이름 (필수)
    private String memberName;
//    회원 이메일    (선택)
    private String memberEmail;
//    회원 로그인 ID ---local:loginID , kakao: 고유 번호 ID  (필수)
    private String memberLoginId;
//    회원 pwd    (필수)
    private String memberPassword;
//    회원 주소 (필수)
    private String memberRegion;
//    회원 생년월일 (필수)
    private LocalDate memberBirth;
//    회원 권한--- ROLE_USER or ROLE_ADMIN
    private MemberRole memberRole;
//    회원 성별 (필수)
    private String memberGender;
//    회원 전화번호   (선택)
    private String memberPhone;
//    login 방식 kakao or local
    private String memberProvider;
//    회원 가입 일자
    private LocalDate memberCreatedAt;



}
