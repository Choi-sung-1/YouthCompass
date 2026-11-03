package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.PolicyListRequestDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PolicyMapper {

    int upsertPolicy(PolicyVO policyVO);

    // 전체 정책 (마감 포함)
    List<PolicyVO> findAllPolicies();

    // 모집 중인 정책만 (마감일 없음 또는 오늘 이후)
    List<PolicyVO> findOpenPolicies();

    // 이번 동기화에서 갱신되지 않은 정책을 INACTIVE로 (반환: 비활성화된 개수)
    int deactivateNotSyncedPolicies(LocalDateTime syncStartedAt);

    // 정책 목록 검색 (필터 + 페이징)
    List<PolicyVO> searchPolicies(PolicyListRequestDTO condition);

    // 정책 목록 검색 결과 전체 개수
    int countPolicies(PolicyListRequestDTO condition);
}