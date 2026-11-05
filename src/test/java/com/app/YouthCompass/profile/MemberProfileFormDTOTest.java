package com.app.YouthCompass.profile;

import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.profile.dto.MemberProfileFormDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberProfileFormDTOTest {

    @Test
    void 채운_칸_수를_센다_빈_시군은_안_채운_것() {
        MemberProfileFormDTO form = new MemberProfileFormDTO();
        assertThat(form.filledCount()).isZero();

        form.setSigunguCode("");
        form.setSchool(SchoolRequirement.UNIVERSITY_STUDENT);
        form.setAnnualIncome(0L);   // 소득 0도 입력한 값
        assertThat(form.filledCount()).isEqualTo(2);
        assertThat(form.filledCount()).isLessThanOrEqualTo(MemberProfileFormDTO.TOTAL_FIELDS);
    }
}
