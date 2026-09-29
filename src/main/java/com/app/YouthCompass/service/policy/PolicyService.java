package com.app.YouthCompass.service.policy;

import com.app.YouthCompass.domain.vo.policy.PolicyVO;

import java.util.List;

public interface PolicyService {
    public List<PolicyVO> findAllPolicies();
}
