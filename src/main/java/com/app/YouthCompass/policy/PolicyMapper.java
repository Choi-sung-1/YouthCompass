package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PolicyMapper {
    int upsertPolicy(PolicyVO policyVO);
    List<PolicyVO> findAllPolicies();
}
