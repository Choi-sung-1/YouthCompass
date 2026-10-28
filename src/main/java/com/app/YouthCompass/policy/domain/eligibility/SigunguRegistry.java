package com.app.YouthCompass.policy.domain.eligibility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

// 시·군 단위 코드표 (resources/sigungu.csv)
// - 도: 시·군 단위 (일반구는 소속 시로 합침)
// - 특별시·광역시·세종: 시 전체가 1단위 (예: 11000 서울특별시)
// - 전남광주통합특별시: 광주 5개 구 = "광주"(12200) 1단위, 전남 22개 시·군은 각각
public final class SigunguRegistry {

    public record Unit(String unitCode, String sidoCode, String unitName) {}

    // 5자리 시군구 코드 → 단위
    private static final Map<String, Unit> BY_CODE = new HashMap<>();
    // "시도코드:이름" → 단위 (AI가 뽑은 이름 검색용)
    private static final Map<String, Unit> BY_NAME = new HashMap<>();
    // 시도코드 → 그 시도의 단위 개수 (도 전체 정책 판별용)
    private static final Map<String, Integer> UNIT_COUNT = new HashMap<>();

    static {
        load();
    }

    private SigunguRegistry() {}

    private static void load() {
        InputStream in = SigunguRegistry.class.getResourceAsStream("/sigungu.csv");
        if (in == null) {
            throw new IllegalStateException("sigungu.csv 를 찾을 수 없습니다.");
        }
        Map<String, Set<String>> unitsBySido = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {

            reader.readLine(); // 헤더
            String line;
            while ((line = reader.readLine()) != null) {
                // code,sidoCode,sidoName,name,unitCode,unitName,note
                String[] c = line.split(",", -1);
                Unit unit = new Unit(c[4], c[1], c[5]);
                BY_CODE.put(c[0], unit);

                if (!"legacy".equals(c[6])) {
                    unitsBySido.computeIfAbsent(c[1], k -> new HashSet<>()).add(c[4]);
                    indexName(unit, c[5]);   // 평택시, 광주
                    indexName(unit, c[3]);   // 원래 이름: 성남시분당구, 강남구
                    if (c[3].startsWith(c[5]) && c[3].length() > c[5].length()) {
                        indexName(unit, c[3].substring(c[5].length())); // 분당구
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        unitsBySido.forEach((sido, units) -> UNIT_COUNT.put(sido, units.size()));
    }

    private static void indexName(Unit unit, String name) {
        String key = normalize(name);
        if (key.isEmpty()) {
            return;
        }
        BY_NAME.putIfAbsent(unit.sidoCode() + ":" + key, unit);
        String shortKey = stripSuffix(key);
        if (!shortKey.equals(key)) {
            BY_NAME.putIfAbsent(unit.sidoCode() + ":" + shortKey, unit);
        }
    }

    private static String normalize(String name) {
        return name == null ? "" : name.replaceAll("\\s", "");
    }

    // 평택시 → 평택, 가평군 → 가평, 분당구 → 분당 (중구처럼 한 글자만 남으면 그대로)
    private static String stripSuffix(String name) {
        if (name.length() > 2 && (name.endsWith("시") || name.endsWith("군") || name.endsWith("구"))) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    // 정책 지역코드(5자리 이상) → 단위. 시도 전체 코드(41000 등)나 모르는 코드는 empty
    public static Optional<Unit> fromCode(String code) {
        if (code == null || code.trim().length() < 5) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.trim().substring(0, 5)));
    }

    // 단위코드 → 단위 (회원 DB에 저장된 값 복원용)
    public static Optional<Unit> fromUnitCode(String unitCode) {
        if (unitCode == null) {
            return Optional.empty();
        }
        return BY_CODE.values().stream()
                .filter(u -> u.unitCode().equals(unitCode.trim()))
                .findFirst();
    }

    // AI가 뽑은 이름 → 단위. sidoCode를 알면 그 시도 안에서만 찾음 (고성군: 강원/경남 구분)
    public static Optional<Unit> findByName(String sidoCode, String name) {
        String key = normalize(name);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        if (sidoCode != null) {
            Unit unit = BY_NAME.get(sidoCode + ":" + key);
            if (unit == null) {
                unit = BY_NAME.get(sidoCode + ":" + stripSuffix(key));
            }
            if (unit == null) {
                unit = findByPrefix(sidoCode, key);
            }
            return Optional.ofNullable(unit);
        }
        // 시도를 모르면 전국에서 찾되, 하나로 정해질 때만 반환
        Set<Unit> found = new HashSet<>();
        for (Map.Entry<String, Unit> e : BY_NAME.entrySet()) {
            String n = e.getKey().substring(e.getKey().indexOf(':') + 1);
            if (n.equals(key) || n.equals(stripSuffix(key))) {
                found.add(e.getValue());
            }
        }
        return found.size() == 1 ? found.stream().findFirst() : Optional.empty();
    }

    // "수원영통구" → 수원, "광주광역시" → 광주 : 입력이 아는 이름으로 시작하면 가장 긴 이름을 채택
    private static Unit findByPrefix(String sidoCode, String key) {
        String prefix = sidoCode + ":";
        Unit best = null;
        int bestLength = 1;
        for (Map.Entry<String, Unit> e : BY_NAME.entrySet()) {
            if (!e.getKey().startsWith(prefix)) {
                continue;
            }
            String n = e.getKey().substring(prefix.length());
            if (n.length() > bestLength && key.startsWith(n)) {
                best = e.getValue();
                bestLength = n.length();
            }
        }
        return best;
    }

    // 시도의 단위 개수 (경기 31, 서울 1 …)
    public static int unitCount(String sidoCode) {
        return UNIT_COUNT.getOrDefault(sidoCode, 0);
    }
}
