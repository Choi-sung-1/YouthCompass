package com.app.YouthCompass.policy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyBrowseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 찜 버튼 작업 때 필터 선택지가 빠져서 분야·지역 select가 비어 있던 문제 재발 방지
    @Test
    void 정책_목록에_분야와_지역_선택지가_나온다() throws Exception {
        mockMvc.perform(get("/policies"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryOptions", "regionOptions"))
                .andExpect(content().string(containsString("<option value=\"주거\">주거</option>")))
                .andExpect(content().string(containsString("<option value=\"SEOUL\">서울특별시</option>")));
    }
}
