package com.app.YouthCompass.policy.external.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// APi 전체 응답
public class PolicyApiResponse {

    private int resultCode;
    private String resultMessage;
    private PolicyResult result;
}