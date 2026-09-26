package com.app.YouthCompass.api.policyPublicData.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PolicyApiResponse {

    private int resultCode;
    private String resultMessage;
    private PolicyResult result;
}