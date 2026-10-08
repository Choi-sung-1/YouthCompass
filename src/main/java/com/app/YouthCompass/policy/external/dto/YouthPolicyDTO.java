package com.app.YouthCompass.policy.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouthPolicyDTO {

    // 정책 고유번호
    private String plcyNo;

    // 정책명
    private String plcyNm;

    // 키워드
    private String plcyKywdNm;

    // 정책 설명
    private String plcyExplnCn;

    // 대분류
    private String lclsfNm;

    // 중분류
    private String mclsfNm;

    // 지원 내용
    private String plcySprtCn;

    // 주관 기관
    private String sprvsnInstCdNm;

    // 신청 방법
    private String plcyAplyMthdCn;

    // 신청 URL
    private String aplyUrlAddr;

    // 제출 서류
    private String sbmsnDcmntCn;

    // 참고 URL
    private String refUrlAddr1;


    // ================================
    // 자격 조건
    // ================================

    // 최소 나이
    private String sprtTrgtMinAge;

    // 최대 나이
    private String sprtTrgtMaxAge;

    // 학력 조건 코드
    private String schoolCd;

    // 취업 상태 코드
    private String jobCd;

    // 혼인 상태 코드
    private String mrgSttsCd;

    // 소득 조건 구분 코드
    private String earnCndSeCd;

    // 소득 최소
    private String earnMinAmt;

    // 소득 최대
    private String earnMaxAmt;

    // 소득 기타 조건
    private String earnEtcCn;

    // 전공 코드
    private String plcyMajorCd;

    // 특화 대상 코드
    private String sbizCd;

    // 지역 코드
    private String zipCd;

    // 추가 신청 자격조건
    private String addAplyQlfcCndCn;


    // ================================
    // 등록/수정 정보
    // ================================

    // 최초 등록일
    private String frstRegDt;

    // 최종 수정일
    private String lastMdfcnDt;
}