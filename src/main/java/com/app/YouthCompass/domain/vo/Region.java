package com.app.YouthCompass.domain.vo;
import lombok.Getter;

@Getter
public enum Region {

    GANGWON("51","강원특별자치도"),
    GYEONGGI("41", "경기도"),
    SOUTHGYENGSANG("48", "경상남도"),
    NORTHGYENGSANG("47", "경상북도"),
    GWANGJU("29", "광주광역시"),
    DAEGU("27", "대구광역시"),
    DAEJEON("30", "대전광역시"),
    BUSAN("26", "부산광역시"),
    SEOUL("11", "서울특별시"),
    SEJONG("3611", "세종특별자치시"),
    ULSAN("31", "울산광역시"),
    INCHEON("28", "인천광역시"),
    SOUTHJEOLLA("46","전라남도"),
    NORTHJEOLLA("52", "전북특별자치도"),
    JEJU("50", "제주특별자치도"),
    SOUTHCHUNGCHEONG("44", "충청남도"),
    NORTHCHUNGCHEONG("43", "충청북도");

    private final String regionCode;
    private final String regionName;
    Region(String regionCode, String regionName) {
        this.regionCode = regionCode;
        this.regionName = regionName;
    }

    public static String getRegionByCode(String regionCode) {
        for (Region region : Region.values()) {
            if (region.getRegionCode().equals(regionCode)) {
//                찾았을 경우 지역 이름 반환
                return region.getRegionName();
            }
        }
//        찾지 못했을경우 지역번호 그대로 반환
        return regionCode;
    }
}
