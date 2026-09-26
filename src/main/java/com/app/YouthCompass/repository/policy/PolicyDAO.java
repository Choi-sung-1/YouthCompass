package com.app.YouthCompass.repository.policy;

import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import com.app.YouthCompass.mapper.policy.PolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PolicyDAO {

    private final PolicyMapper policyMapper;

    public int savePolicy(PolicyVO policyVO) {
        return policyMapper.upsertPolicy(policyVO);
    }
}
