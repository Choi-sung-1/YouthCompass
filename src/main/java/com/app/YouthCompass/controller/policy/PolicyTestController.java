package com.app.YouthCompass.controller.policy;
import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import com.app.YouthCompass.service.api.policyPublicData.PolicySyncService;
import com.app.YouthCompass.service.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PolicyTestController {

    private final PolicySyncService policySyncService;
    private final PolicyService policyService;

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
//   모든 데이터 정상 조회하기위한 select 테스트
    @GetMapping("/test/policies")
    @ResponseBody
    public String testPolicies() {

        List<PolicyVO> policies =
                policyService.findAllPolicies();

        if (policies.isEmpty()) {
            return "정책 없음";
        }

        PolicyVO policy = policies.get(0);

        return """
                정책 개수 : %d
                정책명 : %s
                나이 : %s ~ %s
                지역 : %s
                학력 : %s
                취업 : %s
                소득조건 : %s
                전공 : %s
                특화대상 : %s
                """.formatted(
                policies.size(),
                policy.getPolicyName(),
                policy.getPolicyMinAge(),
                policy.getPolicyMaxAge(),
                policy.getPolicyRegionCodes(),
                policy.getPolicySchoolCodes(),
                policy.getPolicyJobCodes(),
                policy.getPolicyIncomeConditionCode(),
                policy.getPolicyMajorCodes(),
                policy.getPolicySpecialTargetCodes()
        );
    }
}