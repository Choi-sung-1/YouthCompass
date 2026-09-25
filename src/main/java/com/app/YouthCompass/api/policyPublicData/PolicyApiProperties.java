package com.app.YouthCompass.api.policyPublicData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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