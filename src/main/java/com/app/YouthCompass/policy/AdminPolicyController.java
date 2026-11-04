package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.external.PolicyDocumentService;
import com.app.YouthCompass.policy.external.PolicySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용: 온통청년 API → DB 정책 동기화, 제출서류 AI 정리
// SecurityConfig에서 /admin/** 은 ROLE_ADMIN만 접근 가능
// (예전 POST /api/test/policies/sync 를 옮김)
@RestController
@RequiredArgsConstructor
public class AdminPolicyController {

    private final PolicySyncService policySyncService;
    private final PolicyDocumentService policyDocumentService;

    @PostMapping(value = "/admin/policies/sync", produces = "text/plain;charset=UTF-8")
    public String syncPolicies() {
        int count = policySyncService.syncPolicies();
        return count + "개 정책 동기화 완료";
    }

    // 제출서류 원문 → 서류 목록 (AI 호출, 비용 발생) — 원문이 새로 생겼거나 바뀐 정책만, 한 번에 최대 limit개
    // 동기화 후에 실행. 대상이 많으면 여러 번 실행하면 이어서 처리됨
    @PostMapping(value = "/admin/policies/documents", produces = "text/plain;charset=UTF-8")
    public String organizeDocuments(@RequestParam(defaultValue = "100") int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 1000);
        PolicyDocumentService.Result result = policyDocumentService.organize(safeLimit);

        String message = "이번 실행 대상 " + result.targets() + "개 중 " + result.saved() + "개 정리 완료";
        if (result.failedBatches() > 0) {
            message += " (실패 묶음 " + result.failedBatches() + "개, 다시 실행하면 이어서 처리)";
        }
        return message;
    }
}