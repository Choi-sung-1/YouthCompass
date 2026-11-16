package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.dto.HomeWidgetDTO;
import com.app.YouthCompass.policy.dto.PolicyListItemDTO;
import com.app.YouthCompass.policy.dto.PolicyListRequestDTO;
import com.app.YouthCompass.policy.dto.PolicyListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    //    온통청년에 같은 사업이 해마다 새 번호로 다시 등록되고 옛 번호도 모집 중으로 남아 있어서
    //    중복은 가장 최근 등록 하나만 남김 (같은 정책이 추천 칸을 두 번 차지하지 않게)
    public List<PolicyVO> findOpenPolicies() {
        return withoutDuplicates(policyMapper.findOpenPolicies());
    }

    //    같은 사업 = 정책명 + 지역이 같음
    //    (이름만 보면 지자체마다 따로 하는 같은 이름 사업이 합쳐지고, URL은 다시 등록할 때마다 바뀌기도 함)
    static List<PolicyVO> withoutDuplicates(List<PolicyVO> policies) {

        Map<String, PolicyVO> latest = new LinkedHashMap<>();

        for (PolicyVO policy : policies) {
            latest.merge(sameProgramKey(policy), policy,
                    (kept, other) -> isNewer(other, kept) ? other : kept);
        }
        return new ArrayList<>(latest.values());
    }

    private static String sameProgramKey(PolicyVO p) {

        String name = p.getPolicyName() == null ? "" : p.getPolicyName().replaceAll("\\s+", "");
        if (name.isEmpty()) {
            return "id:" + p.getPolicyId();   // 이름이 없으면 같은 사업인지 알 수 없으니 합치지 않음
        }

        // 지역은 자격 판정과 같은 시·군 단위로 비교 (순서 차이 무시)
        // 행정구역 개편으로 코드가 바뀌어도 같은 곳이면 같은 지역 (예: 화성시 41590 → 화성시 동탄구 41597, 인천 신설 구)
        String regions = p.getPolicyRegionCodes() == null ? "" : Arrays.stream(p.getPolicyRegionCodes().split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(code -> SigunguRegistry.fromCode(code).map(SigunguRegistry.Unit::unitCode).orElse(code))
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));

        return name + "|" + regions;
    }

    //    외부 ID 앞 8자리가 등록일(yyyyMMdd)이라 문자열로 비교하면 최근 등록이 더 큼
    private static boolean isNewer(PolicyVO a, PolicyVO b) {

        Comparator<PolicyVO> registered = Comparator
                .comparing(PolicyVO::getPolicyExternalId, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(PolicyVO::getPolicyId, Comparator.nullsFirst(Comparator.naturalOrder()));

        return registered.compare(a, b) > 0;
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