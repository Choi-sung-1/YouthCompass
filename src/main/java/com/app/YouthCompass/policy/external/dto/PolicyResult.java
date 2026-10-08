package com.app.YouthCompass.policy.external.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
// 실제 정책 데이터가 들어간 중간상자
public class PolicyResult {

    private PolicyPaging pagging;
    private List<YouthPolicyDTO> youthPolicyList;
}