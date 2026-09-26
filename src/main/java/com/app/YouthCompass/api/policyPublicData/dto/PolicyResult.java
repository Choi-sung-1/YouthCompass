package com.app.YouthCompass.api.policyPublicData.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PolicyResult {

    private PolicyPaging pagging;
    private List<YouthPolicyDTO> youthPolicyList;
}