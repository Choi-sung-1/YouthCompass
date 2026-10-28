package com.app.YouthCompass.policy.domain.eligibility;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public enum Region {

    GANGWON(List.of("51"), "강원특별자치도"),
    GYEONGGI(List.of("41"), "경기도"),
    SOUTHGYEONGSANG(List.of("48"), "경상남도"),
    NORTHGYEONGSANG(List.of("47"), "경상북도"),

    // 기존 코드 + 현재 데이터 코드 대응은 확인된 것부터 추가
    DAEGU(List.of("27"), "대구광역시"),
    DAEJEON(List.of("30"), "대전광역시"),
    BUSAN(List.of("26"), "부산광역시"),
    SEOUL(List.of("11"), "서울특별시"),
    SEJONG(List.of("36"), "세종특별자치시"),
    ULSAN(List.of("31"), "울산광역시"),
    INCHEON(List.of("28"), "인천광역시"),
    JEONNAM_GWANGJU(List.of("12", "29", "46"), "전남광주통합특별시(광주·전남)"),
    NORTHJEOLLA(List.of("52"), "전북특별자치도"),
    JEJU(List.of("50"), "제주특별자치도"),
    SOUTHCHUNGCHEONG(List.of("44"), "충청남도"),
    NORTHCHUNGCHEONG(List.of("43"), "충청북도");

    private final List<String> regionCodes;
    private final String regionName;

    Region(List<String> regionCodes, String regionName) {
        this.regionCodes = regionCodes;
        this.regionName = regionName;
    }

    public static Optional<Region> fromCode(String code) {

        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        String normalizedCode = code.trim();

        return Arrays.stream(values())
                .filter(region ->
                        region.regionCodes.stream()
                                .anyMatch(normalizedCode::startsWith)
                )
                .findFirst();
    }
}