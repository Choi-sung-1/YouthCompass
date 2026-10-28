package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.*;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.eligibility.PolicyEligibilityService;
import com.app.YouthCompass.policy.recommendation.PolicyRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;

// ============================================================
// 추천 정확도 확인용 테스트 페이지 (개발용, AI 호출 없음 → 비용 0)
// GET /api/test/recommend-debug
//  - 조건을 직접 골라서 추천 결과를 표로 확인
//  - 조건별로 몇 개 정책이 탈락했는지, 탈락 정책은 왜 빠졌는지 확인
// ============================================================
@RestController
public class RecommendDebugController {

    private final PolicyService policyService;
    private final PolicyEligibilityService eligibilityService;
    private final PolicyRecommendationService recommendationService;

    public RecommendDebugController(PolicyService policyService,
                                    PolicyEligibilityService eligibilityService,
                                    PolicyRecommendationService recommendationService) {
        this.policyService = policyService;
        this.eligibilityService = eligibilityService;
        this.recommendationService = recommendationService;
    }

    // 조건 이름 (표 헤더, 탈락 사유에 사용)
    private static final String[] CONDITION_NAMES =
            {"나이", "지역", "학력", "취업", "혼인", "소득", "전공", "특화"};

    @GetMapping(value = "/api/test/recommend-debug", produces = "text/html;charset=UTF-8")
    public String debug(
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) String sigungu,
            @RequestParam(required = false) SchoolRequirement school,
            @RequestParam(required = false) JobRequirement job,
            @RequestParam(required = false) MarriageRequirement marriage,
            @RequestParam(required = false) Long income,
            @RequestParam(required = false) MajorRequirement major,
            @RequestParam(required = false) Set<SpecialTargetRequirement> special,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "false") boolean showExcluded) {

        // ---------- 1. 시·군 이름 → 코드 ----------
        String sigunguCode = null;
        String sigunguMessage = "입력 안 함 (시·도 기준으로 판정)";
        if (notBlank(sigungu)) {
            String sidoCode = region == null ? null : region.getRegionCodes().get(0);
            Optional<SigunguRegistry.Unit> unit = SigunguRegistry.findByName(sidoCode, sigungu.trim());
            if (unit.isPresent()) {
                sigunguCode = unit.get().unitCode();
                sigunguMessage = unit.get().unitName() + " (" + sigunguCode + ")";
                if (region == null) {
                    region = Region.fromCode(unit.get().sidoCode()).orElse(null);
                }
            } else {
                sigunguMessage = "'" + sigungu + "' 인식 실패 → 시·도 기준으로 판정";
            }
        }

        UserPolicyProfileVO user = UserPolicyProfileVO.builder()
                .age(age)
                .region(region)
                .sigunguCode(sigunguCode)
                .school(school)
                .job(job)
                .marriage(marriage)
                .annualIncome(income)
                .major(major)
                .specialTargets(special)
                .build();

        PolicySearchIntent intent = PolicySearchIntent.builder()
                .category(blankToNull(category))
                .keyword(blankToNull(keyword))
                .build();

        // ---------- 2. 필터링 통계 ----------
        int activeCount = policyService.findAllPolicies().size();
        List<PolicyVO> openPolicies = policyService.findOpenPolicies();

        int[] rejectByCondition = new int[CONDITION_NAMES.length];
        List<PolicyEvaluationResult> rejected = new ArrayList<>();
        int passed = 0;

        for (PolicyVO policy : openPolicies) {
            PolicyEvaluationResult result = eligibilityService.evaluate(user, policy);
            EligibilityStatus[] statuses = statuses(result);
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i] == EligibilityStatus.NOT_MATCH) {
                    rejectByCondition[i]++;
                }
            }
            if (result.getNotMatchCount() == 0) {
                passed++;
            } else {
                rejected.add(result);
            }
        }

        // ---------- 3. 실제 추천 결과 (서비스 그대로 호출) ----------
        List<PolicyEvaluationResult> recommended = recommendationService.recommend(user, intent);

        // ---------- 4. HTML ----------
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang='ko'><head><meta charset='utf-8'>")
            .append("<title>추천 디버그</title>").append(STYLE).append("</head><body>");

        html.append("<h1>추천 정확도 확인 <small>AI 호출 없음 · 개발용</small></h1>");

        appendForm(html, age, region, sigungu, school, job, marriage, income, major,
                special, category, keyword, limit, showExcluded);

        // 입력 해석
        html.append("<div class='box'><b>판정에 사용된 조건</b> · ")
            .append("나이 ").append(show(age))
            .append(" · 지역 ").append(region == null ? "-" : esc(region.getRegionName()))
            .append(" / 시·군 ").append(esc(sigunguMessage))
            .append(" · 학력 ").append(desc(school, SchoolRequirement::getDescription))
            .append(" · 취업 ").append(desc(job, JobRequirement::getDescription))
            .append(" · 혼인 ").append(desc(marriage, MarriageRequirement::getDescription))
            .append(" · 소득 ").append(income == null ? "-" : income + "만원")
            .append(" · 전공 ").append(desc(major, MajorRequirement::getDescription))
            .append(" · 특화 ").append(special == null || special.isEmpty() ? "-" : esc(joinDesc(special)))
            .append(" · 분야 ").append(show(blankToNull(category)))
            .append(" · 키워드 ").append(show(blankToNull(keyword)))
            .append("</div>");

        // 깔때기
        html.append("<div class='funnel'>")
            .append(stat("활성 정책", activeCount, ""))
            .append(stat("모집 중", openPolicies.size(), "마감 " + (activeCount - openPolicies.size()) + "개 제외"))
            .append(stat("자격 통과", passed, "탈락 " + rejected.size() + "개"))
            .append(stat("화면 표시", Math.min(limit, recommended.size()), "추천 순위 상위"))
            .append("</div>");

        // 조건별 탈락 수
        html.append("<h2>조건별 탈락 수 <small>모집 중 ").append(openPolicies.size())
            .append("개 중 그 조건이 NOT_MATCH인 정책 수 (한 정책이 여러 조건에서 탈락할 수 있음)</small></h2>")
            .append("<table class='narrow'><tr>");
        for (String name : CONDITION_NAMES) {
            html.append("<th>").append(name).append("</th>");
        }
        html.append("</tr><tr>");
        for (int count : rejectByCondition) {
            html.append("<td class='num'>").append(count).append("</td>");
        }
        html.append("</tr></table>");

        // 추천 TOP N
        html.append("<h2>추천 결과 TOP ").append(Math.min(limit, recommended.size()))
            .append(" <small>정렬: 관련도 ↓ → 확인필요 ↑ → 자격점수 ↓ (PolicyRecommendationService 그대로)</small></h2>");
        appendResultTable(html, recommended.subList(0, Math.min(limit, recommended.size())), true);

        // 탈락 정책
        if (showExcluded) {
            List<PolicyEvaluationResult> shown = rejected;
            String note = "앞에서부터 50개";
            if (notBlank(keyword)) {
                String k = keyword.trim();
                shown = rejected.stream()
                        .filter(r -> contains(r.getPolicy().getPolicyName(), k)
                                || contains(r.getPolicy().getPolicyKeywords(), k))
                        .toList();
                note = "키워드 '" + esc(k) + "'가 들어간 탈락 정책";
            }
            html.append("<h2>탈락한 정책 <small>").append(note).append(" · ")
                .append(shown.size()).append("개 · 빨간 칸이 탈락 사유</small></h2>");
            appendResultTable(html, shown.subList(0, Math.min(50, shown.size())), false);
        }

        html.append("</body></html>");
        return html.toString();
    }

    // ============================================================
    // 결과 표
    // ============================================================
    private void appendResultTable(StringBuilder html, List<PolicyEvaluationResult> results, boolean ranked) {

        if (results.isEmpty()) {
            html.append("<p class='empty'>결과 없음</p>");
            return;
        }

        html.append("<table><tr>")
            .append(ranked ? "<th>#</th>" : "")
            .append("<th>정책명 / 기관</th><th>분류</th><th>대상 지역</th><th>나이</th><th>소득</th><th>신청기간</th>");
        for (String name : CONDITION_NAMES) {
            html.append("<th class='st'>").append(name).append("</th>");
        }
        html.append(ranked ? "<th>관련도</th><th>자격점수</th><th>확인필요</th>" : "<th>탈락 사유</th>")
            .append("</tr>");

        int rank = 1;
        for (PolicyEvaluationResult r : results) {
            PolicyVO p = r.getPolicy();
            String url = notBlank(p.getPolicyApplicationUrl()) ? p.getPolicyApplicationUrl() : p.getPolicyReferenceUrl();

            html.append("<tr>");
            if (ranked) {
                html.append("<td class='num'>").append(rank++).append("</td>");
            }
            html.append("<td class='name'>");
            if (notBlank(url)) {
                html.append("<a href='").append(esc(url)).append("' target='_blank'>").append(esc(p.getPolicyName())).append("</a>");
            } else {
                html.append(esc(p.getPolicyName()));
            }
            html.append("<div class='sub'>").append(esc(nz(p.getPolicyOrganizationName()))).append("</div></td>")
                .append("<td>").append(esc(nz(p.getPolicyLargeCategory())))
                .append("<div class='sub'>").append(esc(nz(p.getPolicyMediumCategory()))).append("</div></td>")
                .append("<td>").append(esc(regionSummary(p.getPolicyRegionCodes()))).append("</td>")
                .append("<td>").append(ageRange(p)).append("</td>")
                .append("<td>").append(esc(incomeSummary(p))).append("</td>")
                .append("<td>").append(applyPeriod(p)).append("</td>");

            EligibilityStatus[] statuses = statuses(r);
            List<String> reasons = new ArrayList<>();
            for (int i = 0; i < statuses.length; i++) {
                html.append(statusCell(statuses[i]));
                if (statuses[i] == EligibilityStatus.NOT_MATCH) {
                    reasons.add(CONDITION_NAMES[i]);
                }
            }

            if (ranked) {
                html.append("<td class='num'>").append(r.getRelevanceScore()).append("</td>")
                    .append("<td class='num'>").append(r.getSpecificMatchScore()).append("</td>")
                    .append("<td class='num'>").append(r.getUnknownCount()).append("</td>");
            } else {
                html.append("<td>").append(String.join(", ", reasons)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
    }

    private EligibilityStatus[] statuses(PolicyEvaluationResult r) {
        return new EligibilityStatus[]{
                r.getAgeResult(), r.getRegionResult(), r.getSchoolResult(), r.getJobResult(),
                r.getMarriageResult(), r.getIncomeResult(), r.getMajorResult(), r.getSpecialTargetResult()};
    }

    private String statusCell(EligibilityStatus status) {
        if (status == EligibilityStatus.MATCH) {
            return "<td class='st ok'>✓</td>";
        }
        if (status == EligibilityStatus.NOT_MATCH) {
            return "<td class='st no'>✗</td>";
        }
        return "<td class='st unk'>?</td>";
    }

    // ============================================================
    // 정책 정보 요약
    // ============================================================

    // 지역코드 목록 → "경기도 전체", "경기도 평택시·안성시", "서울특별시 전체 외 2개 시·도"
    private String regionSummary(String codes) {

        if (!notBlank(codes)) {
            return "전국 (제한 없음)";
        }

        Map<String, String> sidoNames = new LinkedHashMap<>();
        Map<String, Set<String>> unitsBySido = new HashMap<>();
        Set<String> wholeSido = new HashSet<>();
        int unknown = 0;

        for (String raw : codes.split(",")) {
            String code = raw.trim();
            if (code.isEmpty()) {
                continue;
            }
            Optional<Region> region = Region.fromCode(code);
            if (region.isEmpty()) {
                unknown++;
                continue;
            }
            String sidoCode = region.get().getRegionCodes().get(0);
            sidoNames.putIfAbsent(sidoCode, region.get().getRegionName());

            Optional<SigunguRegistry.Unit> unit = SigunguRegistry.fromCode(code);
            if (unit.isEmpty()) {
                wholeSido.add(sidoCode);
            } else {
                unitsBySido.computeIfAbsent(sidoCode, k -> new LinkedHashSet<>()).add(unit.get().unitName());
            }
        }

        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> sido : sidoNames.entrySet()) {
            Set<String> units = unitsBySido.getOrDefault(sido.getKey(), Set.of());
            if (wholeSido.contains(sido.getKey()) || units.size() >= SigunguRegistry.unitCount(sido.getKey())) {
                parts.add(sido.getValue() + " 전체");
            } else {
                List<String> names = new ArrayList<>(units);
                String shown = String.join("·", names.subList(0, Math.min(3, names.size())));
                parts.add(sido.getValue() + " " + shown + (names.size() > 3 ? " 외 " + (names.size() - 3) : ""));
            }
        }

        String summary = parts.size() > 2
                ? String.join(", ", parts.subList(0, 2)) + " 외 " + (parts.size() - 2) + "개 시·도"
                : String.join(", ", parts);
        if (unknown > 0) {
            summary += " (해석불가 " + unknown + ")";
        }
        return summary.isEmpty() ? "해석불가" : summary;
    }

    private String ageRange(PolicyVO p) {
        Integer min = p.getPolicyMinAge();
        Integer max = p.getPolicyMaxAge();
        boolean noMin = min == null || min <= 0;
        boolean noMax = max == null || max <= 0;
        if (noMin && noMax) {
            return "제한없음";
        }
        return (noMin ? "" : min) + "~" + (noMax ? "" : max);
    }

    private String incomeSummary(PolicyVO p) {
        IncomeConditionType type = IncomeConditionType.fromCode(p.getPolicyIncomeConditionCode());
        if (type != IncomeConditionType.ANNUAL_INCOME) {
            return p.getPolicyIncomeConditionCode() == null ? "-" : type.getDescription();
        }
        Long min = p.getPolicyIncomeMinAmount();
        Long max = p.getPolicyIncomeMaxAmount();
        boolean noMin = min == null || min <= 0;
        boolean noMax = max == null || max <= 0;
        if (noMin && noMax) {
            return "연소득 (금액 없음)";
        }
        return "연소득 " + (noMin ? "" : min) + "~" + (noMax ? "" : max) + "만원";
    }

    private String applyPeriod(PolicyVO p) {
        if (p.getPolicyApplyEndDate() == null) {
            return "상시/미정";
        }
        return (p.getPolicyApplyStartDate() == null ? "" : p.getPolicyApplyStartDate().toString().substring(5))
                + "~" + p.getPolicyApplyEndDate();
    }

    // ============================================================
    // 입력 폼
    // ============================================================
    private void appendForm(StringBuilder html, Integer age, Region region, String sigungu,
                            SchoolRequirement school, JobRequirement job, MarriageRequirement marriage,
                            Long income, MajorRequirement major, Set<SpecialTargetRequirement> special,
                            String category, String keyword, int limit, boolean showExcluded) {

        html.append("<form method='get'><div class='grid'>");

        html.append(label("나이")).append("<input type='number' name='age' value='").append(age == null ? "" : age).append("'>");

        html.append(label("시·도")).append("<select name='region'><option value=''>-</option>");
        for (Region r : Region.values()) {
            html.append(option(r.name(), r.getRegionName(), r == region));
        }
        html.append("</select>");

        html.append(label("시·군")).append("<input name='sigungu' placeholder='예: 평택, 분당, 광주' value='")
            .append(esc(nz(sigungu))).append("'>");

        html.append(label("학력")).append(select("school", SchoolRequirement.values(), school, SchoolRequirement::getDescription));
        html.append(label("취업")).append(select("job", JobRequirement.values(), job, JobRequirement::getDescription));
        html.append(label("혼인")).append(select("marriage", MarriageRequirement.values(), marriage, MarriageRequirement::getDescription));

        html.append(label("연소득(만원)")).append("<input type='number' name='income' value='")
            .append(income == null ? "" : income).append("'>");

        html.append(label("전공")).append(select("major", MajorRequirement.values(), major, MajorRequirement::getDescription));

        html.append(label("분야(대분류)")).append("<input name='category' placeholder='예: 주거, 일자리' value='")
            .append(esc(nz(category))).append("'>");
        html.append(label("키워드")).append("<input name='keyword' placeholder='예: 월세' value='")
            .append(esc(nz(keyword))).append("'>");

        html.append(label("표시 개수")).append("<input type='number' name='limit' value='").append(limit).append("'>");
        html.append("</div><div class='checks'><b>특화 대상</b> ");
        for (SpecialTargetRequirement t : SpecialTargetRequirement.values()) {
            if (!isUserValue(t)) {
                continue;
            }
            boolean checked = special != null && special.contains(t);
            html.append("<label><input type='checkbox' name='special' value='").append(t.name()).append("'")
                .append(checked ? " checked" : "").append("> ").append(esc(t.getDescription())).append("</label>");
        }
        html.append("</div><div class='checks'><label><input type='checkbox' name='showExcluded' value='true'")
            .append(showExcluded ? " checked" : "")
            .append("> 탈락한 정책도 보기 (키워드를 넣으면 그 키워드가 들어간 탈락 정책만)</label> ")
            .append("<button type='submit'>추천 보기</button> <a href='?'>초기화</a></div></form>");
    }

    private <E extends Enum<E>> String select(String name, E[] values, E selected, Function<E, String> description) {
        StringBuilder s = new StringBuilder("<select name='").append(name).append("'><option value=''>-</option>");
        for (E value : values) {
            if (isUserValue(value)) {
                s.append(option(value.name(), description.apply(value), value == selected));
            }
        }
        return s.append("</select>").toString();
    }

    private String option(String value, String text, boolean selected) {
        return "<option value='" + value + "'" + (selected ? " selected" : "") + ">" + esc(text) + "</option>";
    }

    private String label(String text) {
        return "<label class='lb'>" + text + "</label>";
    }

    private String stat(String title, int value, String sub) {
        return "<div class='stat'><div class='t'>" + title + "</div><div class='v'>" + value
                + "</div><div class='s'>" + esc(sub) + "</div></div>";
    }

    // ============================================================
    // 작은 도우미
    // ============================================================
    private static boolean isUserValue(Enum<?> value) {
        return !value.name().equals("UNKNOWN") && !value.name().equals("NO_RESTRICTION");
    }

    private <E extends Enum<E>> String desc(E value, Function<E, String> description) {
        return value == null ? "-" : esc(description.apply(value));
    }

    private String joinDesc(Set<SpecialTargetRequirement> targets) {
        return String.join(", ", targets.stream().map(SpecialTargetRequirement::getDescription).toList());
    }

    private String show(Object value) {
        return value == null ? "-" : esc(String.valueOf(value));
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.contains(keyword);
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String blankToNull(String value) {
        return notBlank(value) ? value.trim() : null;
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("'", "&#39;").replace("\"", "&quot;");
    }

    private static final String STYLE = """
            <style>
            body{font:13px/1.45 -apple-system,'Apple SD Gothic Neo',sans-serif;margin:20px;color:#1d2433;background:#fff}
            h1{font-size:20px;margin:0 0 12px} h2{font-size:15px;margin:22px 0 8px}
            small{color:#6b7280;font-weight:normal;font-size:12px}
            form{background:#f6f7f9;border:1px solid #e3e6ea;border-radius:8px;padding:12px}
            .grid{display:grid;grid-template-columns:repeat(6,auto 1fr);gap:6px 8px;align-items:center}
            .lb{color:#4b5563;white-space:nowrap} input,select{padding:4px 6px;border:1px solid #cdd2d8;border-radius:4px;min-width:0}
            .checks{margin-top:8px} .checks label{margin-right:10px;white-space:nowrap}
            button{padding:5px 14px;background:#2f5bd3;color:#fff;border:0;border-radius:4px;cursor:pointer}
            .box{margin:12px 0;padding:8px 10px;background:#eef3ff;border-radius:6px}
            .funnel{display:flex;gap:10px;margin:10px 0}
            .stat{flex:1;border:1px solid #e3e6ea;border-radius:8px;padding:8px 12px}
            .stat .t{color:#6b7280} .stat .v{font-size:22px;font-weight:600} .stat .s{color:#6b7280;font-size:12px}
            table{border-collapse:collapse;width:100%} table.narrow{width:auto}
            th,td{border:1px solid #e3e6ea;padding:5px 7px;vertical-align:top;text-align:left}
            th{background:#f6f7f9;font-weight:600;white-space:nowrap}
            .num{text-align:right} .name{min-width:220px} .sub{color:#6b7280;font-size:12px}
            .st{text-align:center;width:34px} .ok{background:#e6f6ea;color:#17803d} .no{background:#fde8e8;color:#b42318;font-weight:700}
            .unk{background:#fff6dd;color:#9a6b00} .empty{color:#6b7280}
            a{color:#2f5bd3}
            </style>
            """;
}
