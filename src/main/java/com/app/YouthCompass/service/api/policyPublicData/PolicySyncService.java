package com.app.YouthCompass.service.api.policyPublicData;

import com.app.YouthCompass.api.policyPublicData.PolicyApiClient;
import com.app.YouthCompass.api.policyPublicData.dto.PolicyApiResponse;
import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import com.app.YouthCompass.converter.PolicyConverter;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import com.app.YouthCompass.repository.policy.PolicyDAO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicySyncService {

    private final PolicyApiClient policyApiClient;
    private final PolicyConverter policyConverter;
    private final PolicyDAO policyDAO;

    public PolicySyncService(PolicyApiClient policyApiClient, PolicyConverter policyConverter, PolicyDAO policyDAO) {
        this.policyApiClient = policyApiClient;
        this.policyConverter = policyConverter;
        this.policyDAO = policyDAO;
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
            policyDAO.savePolicy(policy);
            count++;
        }
        return count;
    }
}
