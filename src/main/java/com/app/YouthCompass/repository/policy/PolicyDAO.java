package com.app.YouthCompass.repository.policy;

import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import com.app.YouthCompass.mapper.policy.PolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PolicyDAO {

    private final PolicyMapper policyMapper;

//  정책 데이터 insert
    public int savePolicy(PolicyVO policyVO) {
        return policyMapper.upsertPolicy(policyVO);
    }

//  모든 정책 select
    public List<PolicyVO> findAllPolicies(){
        return policyMapper.findAllPolicies();
    }
}
