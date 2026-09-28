package com.app.YouthCompass.service.policy;
import com.app.YouthCompass.domain.vo.member.UserPolicyProfileVO;
import com.app.YouthCompass.domain.vo.policy.EligibilityStatus;
import com.app.YouthCompass.domain.vo.policy.PolicyVO;
import org.springframework.stereotype.Service;

@Service
public class PolicyEligibilityServiceImpl
        implements PolicyEligibilityService {

//  나이 조건 판단
    @Override
    public EligibilityStatus checkAge(UserPolicyProfileVO user, PolicyVO policy) {

        Integer userAge = user.getAge();

        Integer minAge = policy.getPolicyMinAge();
        Integer maxAge = policy.getPolicyMaxAge();

        // 정책에 나이 제한 자체가 없는 경우
        if (minAge == null && maxAge == null) {return EligibilityStatus.MATCH;}


        // 정책에는 나이 조건이 있는데
        // 사용자 나이 정보가 없는 경우
        if (userAge == null) {return EligibilityStatus.UNKNOWN;}

        // 최소 나이 미달
        if (minAge != null && userAge < minAge) {return EligibilityStatus.NOT_MATCH;}

        // 최대 나이 초과
        if (maxAge != null && userAge > maxAge) {return EligibilityStatus.NOT_MATCH;}

        // 모든 나이 조건 통과
        return EligibilityStatus.MATCH;
    }
}