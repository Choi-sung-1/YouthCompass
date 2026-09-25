package com.app.YouthCompass.api.policyPublicData.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PolicyPaging {

    private int totCount;
    private int pageNum;
    private int pageSize;
}