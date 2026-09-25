package com.app.YouthCompass.api.policyPublicData;

import com.app.YouthCompass.api.policyPublicData.dto.PolicyApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

//외부 공공데이터와 통신하여 데이터를 받아오는 클라이언트 모듈
//온통청년 API 한 페이지 호출 담당

@Component
public class PolicyApiClient {

//    Http 통신 클래스
    private final RestClient restClient;
    private final PolicyApiProperties properties;

//    생성자
    public PolicyApiClient(PolicyApiProperties properties) {

        this.properties = properties;

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public PolicyApiResponse getPolicies(int pageNum,int pageSize) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/go/ythip/getPlcy")
                        .queryParam("apiKeyNm", properties.getApiKey())
                        .queryParam("pageNum", pageNum)
                        .queryParam("pageSize", pageSize)
                        .query("resultType=JSON")
                        .build())
                .retrieve()
                .body(PolicyApiResponse.class);
    }
}