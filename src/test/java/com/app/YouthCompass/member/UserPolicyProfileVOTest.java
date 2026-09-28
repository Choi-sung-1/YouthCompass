package com.app.YouthCompass.member;

import com.app.YouthCompass.domain.vo.policy.Region;
import com.app.YouthCompass.domain.vo.member.UserPolicyProfileVO;
import com.app.YouthCompass.domain.vo.policy.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPolicyProfileVOTest {

    @Test
    void 사용자_정책_프로필_생성() {

        UserPolicyProfileVO profile =
                UserPolicyProfileVO.builder()
                        .age(25)
                        .region(Region.GYEONGGI)
                        .school(SchoolRequirement.UNIVERSITY_STUDENT)
                        .job(JobRequirement.UNEMPLOYED)
                        .marriage(MarriageRequirement.SINGLE)
                        .annualIncome(20_000_000L)
                        .major(MajorRequirement.ENGINEERING)
                        .build();

        assertEquals(25, profile.getAge());
        assertEquals(Region.GYEONGGI, profile.getRegion());
        assertEquals(
                SchoolRequirement.UNIVERSITY_STUDENT,
                profile.getSchool()
        );
    }
}