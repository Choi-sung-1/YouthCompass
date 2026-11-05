package com.app.YouthCompass.policy.external;

import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.external.dto.YouthPolicyDTO;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyConverterTest {

    private final PolicyConverter converter = new PolicyConverter();

    private PolicyVO convert(String json) {
        YouthPolicyDTO dto = JsonMapper.builder().build().readValue(json, YouthPolicyDTO.class);
        return converter.convert(dto);
    }

    @Test
    void 참여제한_사업기간_기타사항을_받는다() {
        PolicyVO policy = convert("""
                {
                  "plcyNo": "R2026",
                  "plcyNm": "청년 월세 지원",
                  "ptcpPrpTrgtCn": "  공무원, 본 사업 기수혜자  ",
                  "bizPrdSeCd": "0056001",
                  "bizPrdBgngYmd": "20260101",
                  "bizPrdEndYmd": "20261231",
                  "bizPrdEtcCn": "",
                  "etcMttrCn": "문의: 청년정책과(02-000-0000)"
                }
                """);

        assertThat(policy.getPolicyParticipationRestriction()).isEqualTo("공무원, 본 사업 기수혜자");
        assertThat(policy.getPolicyBusinessStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(policy.getPolicyBusinessEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(policy.getPolicyBusinessPeriodEtc()).isNull();
        assertThat(policy.getPolicyEtcNote()).isEqualTo("문의: 청년정책과(02-000-0000)");
    }

    @Test
    void 사업기간_날짜가_없으면_기타_문구만_남고_해당없음은_null() {
        PolicyVO policy = convert("""
                {
                  "plcyNo": "R2027",
                  "plcyNm": "청년 자격증 응시료 지원",
                  "ptcpPrpTrgtCn": "해당없음",
                  "bizPrdSeCd": "0056002",
                  "bizPrdBgngYmd": "",
                  "bizPrdEndYmd": "",
                  "bizPrdEtcCn": "연중 계속",
                  "etcMttrCn": "-"
                }
                """);

        assertThat(policy.getPolicyParticipationRestriction()).isNull();
        assertThat(policy.getPolicyBusinessStartDate()).isNull();
        assertThat(policy.getPolicyBusinessEndDate()).isNull();
        assertThat(policy.getPolicyBusinessPeriodEtc()).isEqualTo("연중 계속");
        assertThat(policy.getPolicyEtcNote()).isNull();
    }
}
