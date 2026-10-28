package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PolicyMapper {

    int upsertPolicy(PolicyVO policyVO);

    // 전체 정책 (마감 포함)
    List<PolicyVO> findAllPolicies();

    // 모집 중인 정책만 (마감일 없음 또는 오늘 이후)
    List<PolicyVO> findOpenPolicies();
}