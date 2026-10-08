package com.app.YouthCompass.policy.external.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 페이지 정보 Only
public class PolicyPaging {

    private int totCount;
    private int pageNum;
    private int pageSize;
}