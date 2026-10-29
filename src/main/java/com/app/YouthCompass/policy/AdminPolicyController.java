package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.external.PolicySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용: 온통청년 API → DB 정책 동기화
// SecurityConfig에서 /admin/** 은 ROLE_ADMIN만 접근 가능
// (예전 POST /api/test/policies/sync 를 옮김)
@RestController
@RequiredArgsConstructor
public class AdminPolicyController {

    private final PolicySyncService policySyncService;

    @PostMapping(value = "/admin/policies/sync", produces = "text/plain;charset=UTF-8")
    public String syncPolicies() {
        int count = policySyncService.syncPolicies();
        return count + "개 정책 동기화 완료";
    }
}
