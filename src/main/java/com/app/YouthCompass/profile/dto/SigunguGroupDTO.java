package com.app.YouthCompass.profile.dto;

import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SigunguRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 시·군 선택 목록: 시·도 하나 = <optgroup> 하나
public record SigunguGroupDTO(String sidoCode, String sidoName, List<SigunguRegistry.Unit> units) {

    // Region enum 순서대로 (가입 폼 지역 목록과 같은 순서)
    public static List<SigunguGroupDTO> all() {

        Map<String, List<SigunguRegistry.Unit>> bySido = SigunguRegistry.unitsBySido();
        List<SigunguGroupDTO> groups = new ArrayList<>();

        for (Region region : Region.values()) {
            // 가입 폼이 저장하는 값과 같은 기준 (전남광주는 "12")
            String sidoCode = region.getRegionCodes().get(0);
            List<SigunguRegistry.Unit> units = bySido.get(sidoCode);

            if (units != null) {
                groups.add(new SigunguGroupDTO(sidoCode, region.getRegionName(), units));
            }
        }
        return groups;
    }
}