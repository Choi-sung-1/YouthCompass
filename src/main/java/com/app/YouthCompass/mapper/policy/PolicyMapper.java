package com.app.YouthCompass.mapper.policy;

import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PolicyMapper {
    int upsertPolicy(PolicyVO policyVO);
}
