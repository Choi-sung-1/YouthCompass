package com.app.YouthCompass.api.policyPublicData;

import com.app.YouthCompass.api.policyPublicData.dto.PolicyApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PolicyApiClient {

    private final RestClient restClient;
    private final PolicyApiProperties properties;

    public PolicyApiClient(PolicyApiProperties properties) {

        this.properties = properties;

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public PolicyApiResponse getPolicies() {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/go/ythip/getPlcy")
                        .queryParam("apiKeyNm", properties.getApiKey())
                        .queryParam("pageNum", 1)
                        .queryParam("pageSize", 10)
                        .query("resultType=JSON")
                        .build())
                .retrieve()
                .body(PolicyApiResponse.class);
    }
}