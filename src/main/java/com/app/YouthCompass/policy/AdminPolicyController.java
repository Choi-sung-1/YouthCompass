package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.external.PolicyDocumentService;
import com.app.YouthCompass.policy.external.PolicySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용: 온통청년 API → DB 정책 동기화 (+ 이어서 제출서류 AI 정리)
// SecurityConfig에서 /admin/** 은 ROLE_ADMIN만 접근 가능
// (예전 POST /api/test/policies/sync 를 옮김)
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminPolicyController {

    private final PolicySyncService policySyncService;
    private final PolicyDocumentService policyDocumentService;

    // 동기화 → 이어서 제출서류 AI 정리 (원문이 바뀐 정책만이라 평소엔 대상이 거의 없음)
    // documents=false 면 동기화만, 서류 정리가 실패해도 동기화 결과는 그대로 돌려줌
    @PostMapping(value = "/admin/policies/sync", produces = "text/plain;charset=UTF-8")
    public String syncPolicies(
            @RequestParam(defaultValue = "true") boolean documents,
            @RequestParam(defaultValue = "100") int documentLimit
    ) {
        int count = policySyncService.syncPolicies();
        String message = count + "개 정책 동기화 완료";

        if (!documents) {
            return message;
        }

        try {
            return message + "\n서류 정리: " + organizeMessage(policyDocumentService.organize(safeLimit(documentLimit)));
        } catch (RuntimeException e) {
            log.warn("동기화 후 서류 정리 실패 (POST /admin/policies/documents 로 다시 실행)", e);
            return message + "\n서류 정리 실패 — /admin/policies/documents 로 다시 실행해 주세요";
        }
    }

    // 제출서류 원문 → 서류 목록 (AI 호출, 비용 발생) — 원문이 새로 생겼거나 바뀐 정책만, 한 번에 최대 limit개
    // 동기화 때 자동으로 같이 실행됨. 대상이 limit보다 많을 때 이어서 처리하는 용도
    @PostMapping(value = "/admin/policies/documents", produces = "text/plain;charset=UTF-8")
    public String organizeDocuments(@RequestParam(defaultValue = "100") int limit) {
        return organizeMessage(policyDocumentService.organize(safeLimit(limit)));
    }

    private int safeLimit(int limit) {
        return Math.min(Math.max(limit, 1), 1000);
    }

    private String organizeMessage(PolicyDocumentService.Result result) {

        String message = "이번 실행 대상 " + result.targets() + "개 중 " + result.saved() + "개 정리 완료";
        if (result.failedBatches() > 0) {
            message += " (실패 묶음 " + result.failedBatches() + "개, 다시 실행하면 이어서 처리)";
        }
        return message;
    }
}