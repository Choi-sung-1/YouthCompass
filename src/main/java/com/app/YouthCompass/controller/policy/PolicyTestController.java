package com.app.YouthCompass.controller.policy;
import com.app.YouthCompass.api.policyPublicData.dto.YouthPolicyDTO;
import com.app.YouthCompass.service.api.policyPublicData.PolicySyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PolicyTestController {

    private final PolicySyncService policySyncService;

    public PolicyTestController(
            PolicySyncService policySyncService
    ) {
        this.policySyncService = policySyncService;
    }

    @GetMapping("/api/test/policies/all")
    public List<YouthPolicyDTO> getAllPolicies() {

        return policySyncService.getAllPolicies();
    }
}