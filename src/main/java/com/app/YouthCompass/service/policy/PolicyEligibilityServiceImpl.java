package com.app.YouthCompass.service.policy;
import com.app.YouthCompass.domain.vo.member.UserPolicyProfileVO;
import com.app.YouthCompass.domain.vo.policy.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class PolicyEligibilityServiceImpl implements PolicyEligibilityService {

    //  나이 조건 판단
    @Override
    public EligibilityStatus checkAge(UserPolicyProfileVO user, PolicyVO policy) {

        Integer userAge = user.getAge();

        Integer minAge = policy.getPolicyMinAge();
        Integer maxAge = policy.getPolicyMaxAge();

        // 정책에 나이 제한 자체가 없는 경우
        if (minAge == null && maxAge == null) {return EligibilityStatus.MATCH;}

        // 정책에는 나이 조건이 있는데 사용자 나이 정보가 없는 경우
        if (userAge == null) {return EligibilityStatus.UNKNOWN;}

        // 최소 나이 미달
        if (minAge != null && userAge < minAge) {return EligibilityStatus.NOT_MATCH;}

        // 최대 나이 초과
        if (maxAge != null && userAge > maxAge) {return EligibilityStatus.NOT_MATCH;}

        // 모든 나이 조건 통과
        return EligibilityStatus.MATCH;
    }

    //  지역조건 판단
    //  매개변수 사용자가 입력한 조건, 데이터베이스에 저장된 정책
    @Override
    public EligibilityStatus checkRegion(UserPolicyProfileVO user, PolicyVO policy) {

        Region userRegion = user.getRegion();
        String policyRegionCodes = policy.getPolicyRegionCodes();

        // 1. 정책에 지역 제한이 없음
        if(policyRegionCodes ==null || policyRegionCodes.isBlank()){
            return EligibilityStatus.MATCH;
        }

        // 2. 정책에는 지역 제한이 있지만 사용자 지역 정보가 없음
        if (userRegion ==null){
            return EligibilityStatus.UNKNOWN;
        }
        String [] codes = policyRegionCodes.split(",");
        boolean validRegionCodeExists = false;

            for (String code : codes){
                if (code ==null || code.isBlank()){
                    continue;
                }
                Optional<Region> policyRegion = Region.fromCode(code.trim());
    //            알 수 없는 지역 코드
                if (policyRegion.isEmpty()){
                    continue;
                }
                validRegionCodeExists = true;

//                하나라도 사용자 지역과 같으면 MATCH
                if(policyRegion.get() == userRegion){
                    return EligibilityStatus.MATCH;
                }
        }

//            정책에 지역코드는 있는데 하나도 해석하지 못할경우
        if (!validRegionCodeExists){
            return EligibilityStatus.UNKNOWN;
        }
//            정상적인 지역코드는 있지만 사용자 지역과 일치하지 않았을경우
        return EligibilityStatus.NOT_MATCH;
    }

    //  학력조건 판단
    @Override
    public EligibilityStatus checkSchool(UserPolicyProfileVO user, PolicyVO policy) {

        SchoolRequirement userSchool = user.getSchool();

        String policySchoolCodes = policy.getPolicySchoolCodes();

        // 정책에 학력 조건 데이터 자체가 없음
        if (policySchoolCodes == null || policySchoolCodes.isBlank()) {
            return EligibilityStatus.MATCH;
        }

//        학력조건이 여러개인경우
        String[] codes = policySchoolCodes.split(",");

        boolean validCodeExists = false;

        for (String code : codes) {

            if (code == null || code.isBlank()) {
                continue;
            }

            SchoolRequirement requirement =
                    SchoolRequirement.fromCode(
                            code.trim()
                    );

            // 해석할 수 없는 코드 ,알수없는 코드가 있다면
            if (requirement == SchoolRequirement.UNKNOWN) {
                continue;
            }

            validCodeExists = true;

            // 정책이 학력 제한없음 사용자 학력을 몰라도 MATCH
            if (requirement ==
                    SchoolRequirement.NO_RESTRICTION) {
                return EligibilityStatus.MATCH;
            }

            // 사용자 학력 정보가 있고  정책 조건과 일치
            if (userSchool != null &&
                    requirement == userSchool) {
                return EligibilityStatus.MATCH;
            }
        }

        // 정책 코드를 하나도 해석하지 못함
        if (!validCodeExists) {

            return EligibilityStatus.UNKNOWN;
        }

        // 정책에 학력 제한이 있는데 사용자 학력을 모름
        if (userSchool == null) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정상적인 학력 조건이 있지만 사용자와 일치하지 않음
        return EligibilityStatus.NOT_MATCH;
    }

    //  취업 상태 판정
    @Override
    public EligibilityStatus checkJob(UserPolicyProfileVO user, PolicyVO policy) {

        JobRequirement userJob = user.getJob();

        String policyJobCodes =
                policy.getPolicyJobCodes();


        // 1. 정책에 취업상태 조건 데이터가 없음
        if (policyJobCodes == null ||
                policyJobCodes.isBlank()) {

            return EligibilityStatus.MATCH;
        }


        String[] codes = policyJobCodes.split(",");

        boolean validCodeExists = false;


        for (String code : codes) {

            if (code == null || code.isBlank()) {
                continue;
            }

            JobRequirement requirement =
                    JobRequirement.fromCode(
                            code.trim()
                    );

            // 해석할 수 없는 코드
            if (requirement == JobRequirement.UNKNOWN) {
                continue;
            }

            validCodeExists = true;

            // 취업상태 제한없음
            if (requirement == JobRequirement.NO_RESTRICTION) {
                return EligibilityStatus.MATCH;
            }

            // 사용자 취업상태와 일치
            if (userJob != null && requirement == userJob) {
                return EligibilityStatus.MATCH;
            }
        }

        // 정책 코드가 존재하지만 하나도 해석하지 못함
        if (!validCodeExists) {

            return EligibilityStatus.UNKNOWN;
        }

        // 정책에 취업상태 제한이 있는데 사용자 취업상태 정보가 없음
        if (userJob == null) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정상적인 정책 조건이 있지만  사용자와 일치하지 않음
        return EligibilityStatus.NOT_MATCH;
    }

    //  혼인 상태 판정
    @Override
    public EligibilityStatus checkMarriage(UserPolicyProfileVO user, PolicyVO policy) {

        MarriageRequirement userMarriage =
                user.getMarriage();

        String policyMarriageCodes = policy.getPolicyMarriageStatusCodes();

        // 1. 정책에 혼인 조건 데이터 자체가 없음
        if (policyMarriageCodes == null || policyMarriageCodes.isBlank()) {

            return EligibilityStatus.MATCH;
        }

        String[] codes = policyMarriageCodes.split(",");

        boolean validCodeExists = false;

        for (String code : codes) {
            if (code == null || code.isBlank()) {
                continue;
            }

            MarriageRequirement requirement =
                    MarriageRequirement.fromCode(
                            code.trim()
                    );

            // 해석할 수 없는 코드
            if (requirement == MarriageRequirement.UNKNOWN) {
                continue;
            }

            validCodeExists = true;

            // 혼인상태 제한없음
            if (requirement == MarriageRequirement.NO_RESTRICTION) {
                return EligibilityStatus.MATCH;
            }

            // 사용자 혼인상태와 일치
            if (userMarriage != null && requirement == userMarriage) {
                return EligibilityStatus.MATCH;
            }
        }

        // 정책 코드는 존재하지만
        // 하나도 해석하지 못함
        if (!validCodeExists) {
             return EligibilityStatus.UNKNOWN;
        }

        // 정책에 혼인조건은 있지만 사용자 혼인정보가 없음
        if (userMarriage == null) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정상적인 조건은 존재하지만 사용자 혼인상태와 일치하지 않음
        return EligibilityStatus.NOT_MATCH;
    }

    //  소득 상태 판정
    @Override
    public EligibilityStatus checkIncome(UserPolicyProfileVO user, PolicyVO policy) {

        Long userAnnualIncome = user.getAnnualIncome();

        String incomeConditionCode = policy.getPolicyIncomeConditionCode();
        Long minIncome = policy.getPolicyIncomeMinAmount();
        Long maxIncome = policy.getPolicyIncomeMaxAmount();


        // 1. 정책에 소득조건 자체가 없음
        if (incomeConditionCode == null || incomeConditionCode.isBlank()) {
            return EligibilityStatus.MATCH;
        }

        IncomeConditionType conditionType =
                IncomeConditionType.fromCode(
                        incomeConditionCode
                );

        // 2. 알 수 없는 소득조건 코드
        if (conditionType == IncomeConditionType.UNKNOWN) {
            return EligibilityStatus.UNKNOWN;
        }

        // 3. 소득 무관
        if (conditionType == IncomeConditionType.NO_RESTRICTION) {
            return EligibilityStatus.MATCH;
        }

        // 4. 기타 소득조건
        // ex) 기준중위소득 120%, 가구소득 등
        // 현재 annualIncome만으로 판정하기 어려움
        if (conditionType == IncomeConditionType.ETC) {
            return EligibilityStatus.UNKNOWN;
        }

        // 5. 연소득 조건
        // 정책에는 조건이 있는데 사용자 소득을 모름
        if (userAnnualIncome == null) {
            return EligibilityStatus.UNKNOWN;
        }

        // 6. 최소소득 조건
        if (minIncome != null && userAnnualIncome < minIncome) {
            return EligibilityStatus.NOT_MATCH;
        }

        // 7. 최대소득 조건
        if (maxIncome != null && userAnnualIncome > maxIncome) {
            return EligibilityStatus.NOT_MATCH;
        }

        // 8. 연소득 조건인데
        // 비교할 최소/최대 금액이 하나도 없음
        if (minIncome == null && maxIncome == null) {
            return EligibilityStatus.UNKNOWN;
        }

        return EligibilityStatus.MATCH;
    }

    //  전공 조건 판정
    @Override
    public EligibilityStatus checkMajor(UserPolicyProfileVO user, PolicyVO policy) {

        MajorRequirement userMajor = user.getMajor();
        String policyMajorCodes = policy.getPolicyMajorCodes();

        // 1. 정책에 전공 조건 데이터가 없음
        if (policyMajorCodes == null || policyMajorCodes.isBlank()) {
            return EligibilityStatus.MATCH;
        }

        String[] codes = policyMajorCodes.split(",");
        boolean validCodeExists = false;

        for (String code : codes) {
            if (code == null || code.isBlank()) {
                continue;
            }

            MajorRequirement requirement = MajorRequirement.fromCode(code.trim());

            // 해석할 수 없는 코드
            if (requirement == MajorRequirement.UNKNOWN) {
                continue;
            }

            validCodeExists = true;

            // 전공 제한 없음
            if (requirement == MajorRequirement.NO_RESTRICTION) {
                return EligibilityStatus.MATCH;
            }

            // 사용자 전공 계열과 일치
            if (userMajor != null && requirement == userMajor) {
                return EligibilityStatus.MATCH;
            }
        }

        // 정책 코드가 있는데 하나도 해석 못함
        if (!validCodeExists) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정책에는 전공 제한이 있는데 사용자 전공 정보가 없음
        if (userMajor == null) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정상적인 전공 조건이 있지만 사용자와 일치하지 않음
        return EligibilityStatus.NOT_MATCH;
    }

    //  특화 대상 조건 판정 ex)장애인,여성,남성...
    @Override
    public EligibilityStatus checkSpecialTarget(UserPolicyProfileVO user, PolicyVO policy) {

        Set<SpecialTargetRequirement> userSpecialTargets = user.getSpecialTargets();
        String policySpecialTargetCodes = policy.getPolicySpecialTargetCodes();

        // 1. 정책에 특화대상 조건 데이터가 없음
        if (policySpecialTargetCodes == null || policySpecialTargetCodes.isBlank()) {
            return EligibilityStatus.MATCH;
        }

        String[] codes = policySpecialTargetCodes.split(",");

        boolean validCodeExists = false;

        for (String code : codes) {

            if (code == null || code.isBlank()) {
                continue;
            }

            SpecialTargetRequirement requirement = SpecialTargetRequirement.fromCode(code.trim());

            // 해석할 수 없는 코드
            if (requirement == SpecialTargetRequirement.UNKNOWN) {
                continue;
            }

            validCodeExists = true;

            // 특화대상 제한없음
            if (requirement == SpecialTargetRequirement.NO_RESTRICTION) {
                return EligibilityStatus.MATCH;
            }

            // 사용자가 해당 특화대상에 포함되는지 확인
            if (userSpecialTargets != null && userSpecialTargets.contains(requirement)) {
                return EligibilityStatus.MATCH;
            }
        }

        // 정책 코드는 있는데 전부 해석하지 못함
        if (!validCodeExists) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정책에는 특화대상 제한이 있는데 사용자 특화대상 정보가 없음
        if (userSpecialTargets == null) {
            return EligibilityStatus.UNKNOWN;
        }

        // 정책의 특화대상과  사용자 특화대상이 하나도 일치하지 않음
        return EligibilityStatus.NOT_MATCH;
    }

    //  최종 조건 판정 ***
    @Override
    public EligibilityStatus evaluate(UserPolicyProfileVO user, PolicyVO policy) {
        EligibilityStatus[] results = {
                checkAge(user, policy),
                checkRegion(user, policy),
                checkSchool(user, policy),
                checkJob(user, policy),
                checkMarriage(user, policy),
                checkIncome(user, policy),
                checkMajor(user, policy),
                checkSpecialTarget(user, policy)
        };

        boolean hasUnknown = false;

        for (EligibilityStatus result : results) {

//          하나라도 조건이 충족하지 않으면 NOT_MATCH
            if (result == EligibilityStatus.NOT_MATCH) {
                return EligibilityStatus.NOT_MATCH;
            }
//          하나라도 확실하지않은 조건이있다면 UNKNOWN
            if (result == EligibilityStatus.UNKNOWN) {
                hasUnknown = true;
            }
        }

//          하나라도 확실하지않은 조건이있다면 UNKNOWN
        if (hasUnknown) {
            return EligibilityStatus.UNKNOWN;
        }
//      모든 조건이 충족하면 MATCH
        return EligibilityStatus.MATCH;
    }

}