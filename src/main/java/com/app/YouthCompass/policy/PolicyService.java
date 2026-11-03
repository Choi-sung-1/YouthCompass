package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.HomeWidgetDTO;
import com.app.YouthCompass.policy.dto.PolicyListItemDTO;
import com.app.YouthCompass.policy.dto.PolicyListRequestDTO;
import com.app.YouthCompass.policy.dto.PolicyListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyMapper policyMapper;
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");
    private static final int CLOSING_SOON_DAYS = 7;
    private static final int RECENT_DAYS = 30;
    private static final int WIDGET_SIZE = 6;

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
    //    홈 위젯: 정책 수 + 마감 임박 + 최근 모집 시작
    public HomeWidgetDTO getHomeWidgets() {

        LocalDate today = LocalDate.now(KOREA);

        // 개수는 목록 검색 조건을 재사용 (목록 화면 "총 N개"와 같은 기준)
        PolicyListRequestDTO all = new PolicyListRequestDTO();
        all.setOpenOnly(false);
        int totalCount = policyMapper.countPolicies(all);
        int openCount = policyMapper.countPolicies(new PolicyListRequestDTO());   // openOnly 기본 true

        List<PolicyListItemDTO> closingSoon = policyMapper
                .findClosingSoon(today, today.plusDays(CLOSING_SOON_DAYS), WIDGET_SIZE)
                .stream().map(PolicyListItemDTO::from).toList();

        List<PolicyListItemDTO> recentlyOpened = policyMapper
                .findRecentlyOpened(today.minusDays(RECENT_DAYS), today, WIDGET_SIZE)
                .stream().map(PolicyListItemDTO::from).toList();

        return new HomeWidgetDTO(totalCount, openCount, closingSoon, recentlyOpened);
    }
}