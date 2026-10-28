package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.EligibilityStatus;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.UserPolicyProfileVO;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegionCheckTest {

    private final PolicyEligibilityService service = new PolicyEligibilityService();

    private UserPolicyProfileVO user(Region region, String sigunguCode) {
        return UserPolicyProfileVO.builder().region(region).sigunguCode(sigunguCode).build();
    }

    private PolicyVO policy(String codes) {
        return PolicyVO.builder().policyRegionCodes(codes).build();
    }

    private EligibilityStatus check(Region region, String sigungu, String codes) {
        return service.checkRegion(user(region, sigungu), policy(codes));
    }

    // 경기도 전체 정책 = sigungu.csv의 경기 시군구 코드 전부
    private String allGyeonggiCodes() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/sigungu.csv"), StandardCharsets.UTF_8))) {
            return reader.lines().skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(c -> c[1].equals("41"))
                    .map(c -> c[0])
                    .collect(Collectors.joining(","));
        }
    }

    // ===== 핵심: 시 구분 =====
    @Test
    void 평택_사용자에게_안양_정책은_NOT_MATCH() {
        assertThat(check(Region.GYEONGGI, "41220", "41171,41173"))
                .isEqualTo(EligibilityStatus.NOT_MATCH);
    }

    @Test
    void 평택_사용자에게_평택_정책은_MATCH() {
        assertThat(check(Region.GYEONGGI, "41220", "41220"))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 경기도_전체_정책은_평택_사용자도_MATCH() throws Exception {
        assertThat(check(Region.GYEONGGI, "41220", allGyeonggiCodes()))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 시도_전체코드_41000도_MATCH() {
        assertThat(check(Region.GYEONGGI, "41220", "41000"))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    // ===== 시를 모를 때 =====
    @Test
    void 경기도까지만_알면_안양_정책은_UNKNOWN() {
        assertThat(check(Region.GYEONGGI, null, "41171"))
                .isEqualTo(EligibilityStatus.UNKNOWN);
    }

    @Test
    void 경기도까지만_알아도_경기도_전체_정책은_MATCH() throws Exception {
        assertThat(check(Region.GYEONGGI, null, allGyeonggiCodes()))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    // ===== 시까지만 규칙 =====
    @Test
    void 수원_사용자에게_영통구_정책은_MATCH() {
        assertThat(check(Region.GYEONGGI, "41110", "41117"))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 서울_사용자에게_강남구_정책은_MATCH() {
        assertThat(check(Region.SEOUL, null, "11680"))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    // ===== 전남광주 통합 =====
    @Test
    void 광주_사용자에게_광산구_정책은_MATCH() {
        assertThat(check(Region.JEONNAM_GWANGJU, "12200", "12330"))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    @Test
    void 광주_사용자에게_여수_정책은_NOT_MATCH() {
        assertThat(check(Region.JEONNAM_GWANGJU, "12200", "12130"))
                .isEqualTo(EligibilityStatus.NOT_MATCH);
    }

    @Test
    void 통합전_광주코드_29155도_광주로_인식() {
        assertThat(check(Region.JEONNAM_GWANGJU, "12200", "29155"))
                .isEqualTo(EligibilityStatus.MATCH);
    }

    // ===== 기존 시도 판정 유지 =====
    @Test
    void 평택_사용자에게_서울_정책은_NOT_MATCH() {
        assertThat(check(Region.GYEONGGI, "41220", "11680"))
                .isEqualTo(EligibilityStatus.NOT_MATCH);
    }

    @Test
    void 지역정보_없으면_UNKNOWN() {
        assertThat(check(null, null, "41220"))
                .isEqualTo(EligibilityStatus.UNKNOWN);
    }
}