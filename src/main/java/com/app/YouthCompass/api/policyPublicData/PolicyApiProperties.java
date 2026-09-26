package com.app.YouthCompass.api.policyPublicData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//application-secret.properties 와 연결하여 apiKey, baseUrl 연결해주는 클래스
@Component
public class PolicyApiProperties {

    @Value("${policy.api.base-url}")
    private String baseUrl;

    @Value("${policy.api.key}")
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}