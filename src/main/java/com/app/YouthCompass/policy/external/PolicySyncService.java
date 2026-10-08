package com.app.YouthCompass.policy.external;

import com.app.YouthCompass.policy.external.dto.PolicyApiResponse;
import com.app.YouthCompass.policy.external.dto.YouthPolicyDTO;
import com.app.YouthCompass.policy.PolicyMapper;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 온통청년 Api 동기화 서비스 로직
@Service
public class PolicySyncService {

    private final PolicyApiClient policyApiClient;
    private final PolicyConverter policyConverter;
    private final PolicyMapper policyMapper;

    public PolicySyncService(PolicyApiClient policyApiClient, PolicyConverter policyConverter, PolicyMapper policyMapper) {
        this.policyApiClient = policyApiClient;
        this.policyConverter = policyConverter;
        this.policyMapper = policyMapper;
    }
//  정책 데이터 받아오기
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
//    정책 동기화
    public int syncPolicies(){
        List<YouthPolicyDTO> allPolicies = getAllPolicies();

        int count = 0;

        for (YouthPolicyDTO dto : allPolicies){
            PolicyVO policy = policyConverter.convert(dto);
            policyMapper.upsertPolicy(policy);
            count++;
        }
        return count;
    }
}
