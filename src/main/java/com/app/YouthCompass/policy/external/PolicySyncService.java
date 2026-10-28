package com.app.YouthCompass.policy.external;

import com.app.YouthCompass.policy.PolicyMapper;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.external.dto.PolicyApiResponse;
import com.app.YouthCompass.policy.external.dto.PolicyResult;
import com.app.YouthCompass.policy.external.dto.YouthPolicyDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// 온통청년 Api 동기화 서비스 로직
@Service
public class PolicySyncService {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 200;      // 무한 반복 방지 (100 x 200 = 2만 건)
    private static final int MAX_ATTEMPTS = 5;     // 페이지당 최대 재시도 횟수

    private final PolicyApiClient policyApiClient;
    private final PolicyConverter policyConverter;
    private final PolicyMapper policyMapper;

    public PolicySyncService(PolicyApiClient policyApiClient, PolicyConverter policyConverter, PolicyMapper policyMapper) {
        this.policyApiClient = policyApiClient;
        this.policyConverter = policyConverter;
        this.policyMapper = policyMapper;
    }

    // 전체 수집 결과
    // complete = API가 알려준 전체 개수만큼 다 받았는지 (비활성화 실행 여부 판단용)
    private record FetchResult(List<YouthPolicyDTO> policies, int totalCount, boolean complete) {}

    //  정책 데이터 받아오기 (테스트 엔드포인트용, 기존과 동일하게 List 반환)
    public List<YouthPolicyDTO> getAllPolicies() {
        return fetchAll().policies();
    }

    //  정책 동기화
    //  1) 전부 받아서 저장(upsert)
    //  2) 전부 받았고 저장 실패가 없을 때만 → 이번에 안 받아진 정책을 INACTIVE 처리
    public int syncPolicies() {

        // 이번 동기화 기준 시각 (DB 정밀도 차이 없게 초 단위로 자름)
        LocalDateTime syncStartedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        FetchResult fetched = fetchAll();

        int success = 0;
        int skipped = 0;
        int failed = 0;

        for (YouthPolicyDTO dto : fetched.policies()) {

            // 필수값(정책번호, 정책명)이 없으면 저장할 수 없음 → 건너뜀
            if (isBlank(dto.getPlcyNo()) || isBlank(dto.getPlcyNm())) {
                skipped++;
                continue;
            }

            try {
                PolicyVO policy = policyConverter.convert(dto);
                policy.setPolicyUpdatedAt(syncStartedAt);
                policyMapper.upsertPolicy(policy);
                success++;

            } catch (RuntimeException e) {
                // 정책 하나 때문에 전체 동기화가 멈추지 않게 기록만 하고 계속
                failed++;
                System.out.println("정책 저장 실패 [" + dto.getPlcyNo() + "] " + e.getMessage());
            }
        }

        System.out.println("동기화 결과 : 저장 " + success + " / 건너뜀 " + skipped + " / 실패 " + failed
                + " / 수집 " + fetched.policies().size() + " / 전체 " + fetched.totalCount());

        // 일부만 받았거나 저장 실패가 있으면, 멀쩡한 정책까지 꺼버릴 수 있으므로 비활성화 생략
        if (fetched.complete() && failed == 0) {
            int deactivated = policyMapper.deactivateNotSyncedPolicies(syncStartedAt);
            System.out.println("API에서 사라진 정책 비활성화 : " + deactivated + "건");
        } else {
            System.out.println("수집이 불완전하거나 저장 실패가 있어 비활성화를 건너뜁니다.");
        }

        return success;
    }

    //  전체 페이지 수집 (null 방어 포함)
    private FetchResult fetchAll() {

        List<YouthPolicyDTO> allPolicies = new ArrayList<>();
        int totalCount = 0;

        for (int pageNum = 1; pageNum <= MAX_PAGES; pageNum++) {

            PolicyApiResponse response = fetchWithRetry(pageNum, PAGE_SIZE);

            // 응답 자체가 비어 있으면 더 진행할 수 없음
            if (response == null || response.getResult() == null) {
                throw new IllegalStateException("페이지 " + pageNum + " 응답이 비어 있습니다.");
            }

            PolicyResult result = response.getResult();
            List<YouthPolicyDTO> policies = result.getYouthPolicyList();

            // 더 받을 정책이 없음
            if (policies == null || policies.isEmpty()) {
                break;
            }
            allPolicies.addAll(policies);

            // 전체 개수는 pagging이 있을 때만 갱신
            if (result.getPagging() != null) {
                totalCount = result.getPagging().getTotCount();
            }
            System.out.println("페이지 : " + pageNum + "/ 수집 : " + allPolicies.size() + "/ 전체 :" + totalCount);

            if (totalCount > 0 && allPolicies.size() >= totalCount) {
                break;
            }
        }

        boolean complete = totalCount > 0 && allPolicies.size() >= totalCount;
        return new FetchResult(allPolicies, totalCount, complete);
    }

    // API 호출: 페이지 사이 0.5초 쉬고, 실패하면 최대 5번까지 재시도
    private PolicyApiResponse fetchWithRetry(int pageNum, int pageSize) {

        for (int attempt = 1; ; attempt++) {
            try {
                Thread.sleep(500);   // 연속 호출로 차단되지 않게 간격 두기
                return policyApiClient.getPolicies(pageNum, pageSize);

            } catch (RestClientException e) {
                System.out.println("페이지 " + pageNum + " 호출 실패 (" + attempt + "/" + MAX_ATTEMPTS + "): "
                        + e.getClass().getSimpleName());
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;   // 5번 다 실패하면 원래대로 에러
                }
                sleepQuietly(2000L * attempt);   // 2초, 4초, 6초, 8초 쉬고 재시도

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("동기화 중단됨", e);
            }
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}