package com.app.YouthCompass.service.api.policyPublicData;

import com.app.YouthCompass.api.policyPublicData.PolicyApiClient;
import com.app.YouthCompass.api.policyPublicData.dto.PolicyApiResponse;
import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicySyncService {

    private final PolicyApiClient policyApiClient;

    public PolicySyncService(PolicyApiClient policyApiClient) {
        this.policyApiClient = policyApiClient;
    }

    public List<YouthPolicyDTO> getAllPolicies(){
        int pageNum = 1;
        int pageSize = 100;

        List<YouthPolicyDTO> allPolicies = new ArrayList<>();

        while (true){
            PolicyApiResponse response = policyApiClient.getPolicies(pageNum, pageSize);
            List<YouthPolicyDTO> policies = response.getResult().getYouthPolicyList();

            if (policies ==null || policies.isEmpty()){
                break;
            }
            allPolicies.addAll(policies);
            int totalCount = response.getResult().getPagging().getTotCount();
            System.out.println("페이지 : "+pageNum + "/ 수집 : "+ allPolicies.size() + "/ 전체 :" + totalCount);

            if (allPolicies.size() >= totalCount){
                break;
            }
            pageNum++;
        }
        return allPolicies;
    }

}
