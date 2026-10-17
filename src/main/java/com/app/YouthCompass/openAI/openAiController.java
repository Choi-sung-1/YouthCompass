package com.app.YouthCompass.openAI;

import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class openAiController {

    private final openAiService service;
    private final PolicyEligibilityService policyEligibilityService;

    @GetMapping("/api/test/ai")
    public UserPolicyProfileVO test() {

        UserPolicyProfileVO userPolicyProfileVO = service.extract("""
                저는 25세이고 안양에 살아요.
                대학 재학중입니다.
                결혼은 안 했어요.
                정보보호학과를 나왔습니다.
                """);

        policyEligibilityService.checkAge()

        return userPolicyProfileVO;
    }

}
