package com.app.YouthCompass.controller.policy;
import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import com.app.YouthCompass.service.api.policyPublicData.PolicySyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PolicyTestController {

    private final PolicySyncService policySyncService;

//    생성자
    public PolicyTestController(PolicySyncService policySyncService) {
        this.policySyncService = policySyncService;
    }
//  정책 데이터 수집 테스트
    @GetMapping("/api/test/policies/all")
    public List<YouthPolicyDTO> getAllPolicies() {

        return policySyncService.getAllPolicies();
    }
//    정책 데이터 수집 및 데이터베이스 upsert(동기화) 테스트
    @PostMapping("/api/test/policies/sync")
    public String syncPolicies() {
        int count = policySyncService.syncPolicies();
        return count + "개 정책 동기화 완료";
    }
}