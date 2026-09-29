package com.app.YouthCompass.service.policy;

import com.app.YouthCompass.domain.vo.member.UserPolicyProfileVO;
import com.app.YouthCompass.domain.vo.policy.EligibilityStatus;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;

public interface PolicyEligibilityService {
//    나이 조건 판단
    EligibilityStatus checkAge(UserPolicyProfileVO user, PolicyVO policy);
//    지역 조건 판정
    EligibilityStatus checkRegion(UserPolicyProfileVO user, PolicyVO policy);
//    학력 조건 판정
    EligibilityStatus checkSchool(UserPolicyProfileVO user ,PolicyVO policy);
//    취업 상태 판정
    EligibilityStatus checkJob(UserPolicyProfileVO user, PolicyVO policy);
//    혼인 상태 판정
    EligibilityStatus checkMarriage(UserPolicyProfileVO user, PolicyVO policy);
//    소득 상태 판정
    EligibilityStatus checkIncome(UserPolicyProfileVO user,PolicyVO policy);
//    전공 조건 판정
    EligibilityStatus checkMajor(UserPolicyProfileVO user, PolicyVO policy);
//    특화 대상 조건 판정
    EligibilityStatus checkSpecialTarget(UserPolicyProfileVO user, PolicyVO policy);

//    **최종 조건 판정**
    EligibilityStatus evaluate(UserPolicyProfileVO user, PolicyVO policy);
}
