package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.PolicyListItemDTO;
import com.app.YouthCompass.policy.dto.PolicyListRequestDTO;
import com.app.YouthCompass.policy.dto.PolicyListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyMapper policyMapper;
    //    모든 정책 조회 (마감 포함)
    public List<PolicyVO> findAllPolicies() {
        return policyMapper.findAllPolicies();
    }

    //    모집 중인 정책만 조회 (추천용)
    public List<PolicyVO> findOpenPolicies() {
        return policyMapper.findOpenPolicies();
    }

    //    정책 목록 검색 (필터 + 페이징)
    public PolicyListResponseDTO searchPolicies(PolicyListRequestDTO condition) {

        int totalCount = policyMapper.countPolicies(condition);

        List<PolicyListItemDTO> items = policyMapper.searchPolicies(condition)
                .stream()
                .map(PolicyListItemDTO::from)
                .toList();

        return new PolicyListResponseDTO(items, totalCount, condition.getPage(), PolicyListRequestDTO.PAGE_SIZE);
    }
}