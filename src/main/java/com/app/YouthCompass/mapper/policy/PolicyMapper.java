package com.app.YouthCompass.mapper.policy;

import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PolicyMapper {
    int upsertPolicy(PolicyVO policyVO);
    List<PolicyVO> findAllPolicies();
}
