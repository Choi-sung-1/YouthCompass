package com.app.YouthCompass.openAI;

import com.app.YouthCompass.policy.domain.eligibility.*;
import com.app.YouthCompass.policy.domain.recommendation.PolicySearchIntent;
import com.app.YouthCompass.policy.recommendation.PolicyRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OpenAiServiceTest {

    @Autowired
    private openAiService openAiService;

    @Autowired
    private PolicyRecommendationService policyRecommendationService;

    private static final String SAMPLE_TEXT = """
            저는 25세이고 안양에 살아요.
            대학 재학중입니다.
            결혼은 안 했어요.
            정보보호학과를 나왔습니다.
            월세 지원 정책을 찾고 있어요
            """;

    // =========================
    // 1. 자연어 → 사용자 조건 추출
    // =========================
    @Test
    @DisplayName("자연어 문장에서 사용자 조건 8개를 추출한다")
    void 자연어_사용자_조건_추출() {

        openAiService.ExtractionResult result = openAiService.extract(SAMPLE_TEXT);
        UserPolicyProfileVO profile = result.profile();

        System.out.println("추출된 프로필 = " + profile);

        assertThat(profile.getAge()).isEqualTo(25);
        assertThat(profile.getRegion()).isEqualTo(Region.GYEONGGI);      // 안양 → 경기도
        assertThat(profile.getSchool()).isEqualTo(SchoolRequirement.UNIVERSITY_STUDENT);
        assertThat(profile.getMarriage()).isEqualTo(MarriageRequirement.SINGLE);
        assertThat(profile.getMajor()).isEqualTo(MajorRequirement.ENGINEERING);
    }

    // =========================
    // 2. 자연어 → 검색 의도 추출
    // =========================
    @Test
    @DisplayName("자연어 문장에서 검색 의도(category, keyword)를 추출한다")
    void 자연어_검색_의도_추출() {

        PolicySearchIntent intent = openAiService.extract(SAMPLE_TEXT).intent();

        System.out.println("category = " + intent.getCategory() + ", keyword = " + intent.getKeyword());

        assertThat(intent.getCategory()).contains("주거");
        assertThat(intent.getKeyword()).contains("월세");
    }

    // =========================
    // 3. 말하지 않은 조건은 null 이어야 함
    //    (현재 annualIncome=0, specialTargets=[UNKNOWN] 이 나와서 실패할 수 있음 → 수정 확인용)
    // =========================
    @Test
    @DisplayName("문장에 없는 조건은 추측하지 않고 null 로 남긴다")
    void 미언급_조건은_null() {

        UserPolicyProfileVO profile = openAiService.extract(SAMPLE_TEXT).profile();

        assertThat(profile.getJob()).isNull();
        assertThat(profile.getAnnualIncome()).isNull();
        assertThat(profile.getSpecialTargets()).isNull();
    }

    // =========================
    // 4. 입력 검증 (AI 호출 없이 바로 예외)
    // =========================
    @Test
    @DisplayName("빈 문장이나 4000자 초과 문장은 예외가 발생한다")
    void 입력_검증() {

        assertThatThrownBy(() -> openAiService.extract("   "))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> openAiService.extract("가".repeat(4001)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================
    // 5. 자연어 추출 → 추천 엔진 연결
    // =========================
    @Test
    @DisplayName("추출 결과로 추천을 실행하면 NOT_MATCH 없는 후보가 관련도 순으로 나온다")
    void 자연어_추출_후_추천() {

        openAiService.ExtractionResult extracted = openAiService.extract(SAMPLE_TEXT);

        List<PolicyEvaluationResult> candidates =
                policyRecommendationService.recommend(extracted.profile(), extracted.intent());

        System.out.println("\n========== 자연어 추천 결과 ==========");
        System.out.println("추출 프로필 = " + extracted.profile());
        System.out.println("검색 의도 = " + extracted.intent().getCategory() + " / " + extracted.intent().getKeyword());
        System.out.println("후보 수 = " + candidates.size());

        int limit = Math.min(10, candidates.size());
        for (int i = 0; i < limit; i++) {
            PolicyEvaluationResult r = candidates.get(i);
            System.out.println((i + 1) + "위"
                    + " | 관련도=" + r.getRelevanceScore()
                    + " | 자격점수=" + r.getSpecificMatchScore()
                    + " | UNKNOWN=" + r.getUnknownCount()
                    + " | 지역판정=" + r.getRegionResult()
                    + " | 대분류=" + r.getPolicy().getPolicyLargeCategory()
                    + " | 정책명=" + r.getPolicy().getPolicyName());
        }

        // 후보가 있어야 함
        assertThat(candidates).isNotEmpty();

        // 모든 후보는 NOT_MATCH 가 0개
        assertThat(candidates)
                .allMatch(r -> r.getNotMatchCount() == 0);

        // 관련도 내림차순 정렬
        for (int i = 1; i < candidates.size(); i++) {
            assertThat(candidates.get(i - 1).getRelevanceScore())
                    .isGreaterThanOrEqualTo(candidates.get(i).getRelevanceScore());
        }

        // 월세를 찾았으니 1위는 관련도 점수가 있어야 함
        assertThat(candidates.get(0).getRelevanceScore()).isGreaterThan(0);
    }
}