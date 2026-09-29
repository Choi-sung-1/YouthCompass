package com.app.YouthCompass.service.policy;

import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import com.app.YouthCompass.repository.policy.PolicyDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {
    private final PolicyDAO policyDAO;

//    모든 정책 조회
    public List<PolicyVO> findAllPolicies(){
        return policyDAO.findAllPolicies();
    }

}
