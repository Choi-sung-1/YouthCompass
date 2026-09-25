package com.app.YouthCompass.controller.policy;

import com.app.YouthCompass.api.policyPublicData.PolicyApiClient;
import com.app.YouthCompass.api.policyPublicData.dto.PolicyApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
public class PolicyTestController {
    private final PolicyApiClient policyApiClient;
//  역 직렬화  JSON 문자열 -->JAVA 객체
    private final ObjectMapper objectMapper;

    public PolicyTestController(PolicyApiClient policyApiClient, ObjectMapper objectMapper) {
        this.policyApiClient = policyApiClient;
        this.objectMapper = objectMapper;
    }


    @GetMapping("/api/test/policies")
    public PolicyApiResponse getPolicies()throws Exception{
        return policyApiClient.getPolicies();
    }
}