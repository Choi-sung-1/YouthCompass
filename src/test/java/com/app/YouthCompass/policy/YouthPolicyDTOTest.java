package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.external.dto.YouthPolicyDTO;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class YouthPolicyDTOTest {

    @Test
    void 신청기간_필드를_받는다() {
        String json = """
                {
                  "plcyNm": "김제청년축제",
                  "aplyPrdSeCd": "0057001",
                  "aplyYmd": "20261030 ~ 20261031"
                }
                """;

        YouthPolicyDTO dto = JsonMapper.builder().build().readValue(json, YouthPolicyDTO.class);

        assertThat(dto.getAplyPrdSeCd()).isEqualTo("0057001");
        assertThat(dto.getAplyYmd()).isEqualTo("20261030 ~ 20261031");
    }
}