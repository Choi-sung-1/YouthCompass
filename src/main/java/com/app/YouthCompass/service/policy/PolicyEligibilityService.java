package com.app.YouthCompass.service.policy;

import com.app.YouthCompass.domain.vo.member.UserPolicyProfileVO;
import com.app.YouthCompass.domain.vo.policy.EligibilityStatus;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;

public interface PolicyEligibilityService {
//    나이 조건 판단
    EligibilityStatus checkAge(UserPolicyProfileVO user, PolicyVO policy);
}
