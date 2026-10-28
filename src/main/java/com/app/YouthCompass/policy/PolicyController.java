package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.eligibility.JobRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MajorRequirement;
import com.app.YouthCompass.policy.domain.eligibility.MarriageRequirement;
import com.app.YouthCompass.policy.domain.eligibility.Region;
import com.app.YouthCompass.policy.domain.eligibility.SchoolRequirement;
import com.app.YouthCompass.policy.domain.eligibility.SpecialTargetRequirement;
import com.app.YouthCompass.policy.dto.ConditionRequestDTO;
import com.app.YouthCompass.policy.dto.RecommendRequestDTO;
import com.app.YouthCompass.policy.dto.RecommendResponseDTO;
import com.app.YouthCompass.policy.recommendation.RecommendResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/policy")
public class PolicyController {

    // AI 프롬프트에 준 대분류 목록과 같음
    private static final List<String> CATEGORIES =
            List.of("일자리", "주거", "교육", "복지문화", "참여권리");

    private final RecommendResultService recommendResultService;

    // 추천 페이지 (입력창만 있는 처음 화면)
    @GetMapping("/recommend")
    public String recommendPage(Model model) {

        model.addAttribute("request", new RecommendRequestDTO());

        return "policy/recommend";
    }

    // 문장 입력 → AI 조건 추출 → 추천 → 상위 10개를 같은 화면에 표시
    @PostMapping("/recommend")
    public String recommend(
            @ModelAttribute("request") RecommendRequestDTO request,
            Model model
    ) {

        try {
            RecommendResponseDTO result =
                    recommendResultService.recommendFromSentence(request.getSentence());

            model.addAttribute("result", result);

            // 12번: AI가 뽑은 조건을 수정 칸에 미리 채워둠
            addConditionForm(
                    model,
                    ConditionRequestDTO.from(result.getProfile(), result.getIntent(), request.getSentence())
            );

        } catch (IllegalArgumentException e) {
            // 빈 문장, 4000자 초과 등 입력 문제 → 메시지를 그대로 보여줌
            model.addAttribute("errorMessage", e.getMessage());

        } catch (RuntimeException e) {
            // AI 호출 실패 등 → 자세한 내용은 로그로만 남김
            log.error("정책 추천 실패", e);
            model.addAttribute(
                    "errorMessage",
                    "추천 중 문제가 생겼어요. 잠시 후 다시 시도해 주세요."
            );
        }

        return "policy/recommend";
    }

    // 12번: 사용자가 고친 조건으로 다시 추천 (AI 호출 없음)
    @PostMapping("/recommend/conditions")
    public String recommendWithConditions(
            @ModelAttribute("conditions") ConditionRequestDTO conditions,
            BindingResult bindingResult,
            Model model
    ) {

        // 입력창에 처음 문장을 그대로 남김
        RecommendRequestDTO request = new RecommendRequestDTO();
        request.setSentence(conditions.getSentence());
        model.addAttribute("request", request);

        String inputError = checkConditions(conditions, bindingResult);
        if (inputError != null) {
            model.addAttribute("conditionError", inputError);
            addConditionForm(model, conditions);
            return "policy/recommend";
        }

        try {
            model.addAttribute(
                    "result",
                    recommendResultService.recommendFromProfile(conditions.toProfile(), conditions.toIntent())
            );
            model.addAttribute("edited", true);

            if (conditions.isSigunguNotFound()) {
                model.addAttribute(
                        "conditionNotice",
                        "'" + conditions.getSigungu() + "'을(를) 찾지 못해서 시·군 없이 추천했어요. "
                                + "시·군 이름(예: 안양시)을 확인해 주세요."
                );
            }

        } catch (RuntimeException e) {
            log.error("조건 수정 후 추천 실패", e);
            model.addAttribute(
                    "errorMessage",
                    "추천 중 문제가 생겼어요. 잠시 후 다시 시도해 주세요."
            );
        }

        addConditionForm(model, conditions);
        return "policy/recommend";
    }

    // 숫자 칸에 숫자가 아닌 값, 말이 안 되는 나이·소득 → 안내 문구
    private String checkConditions(ConditionRequestDTO c, BindingResult bindingResult) {

        if (bindingResult.hasFieldErrors("age")) {
            return "나이는 숫자로 입력해 주세요.";
        }
        if (bindingResult.hasFieldErrors("annualIncome")) {
            return "연소득은 만원 단위 숫자로 입력해 주세요. (예: 2400)";
        }
        if (bindingResult.hasErrors()) {
            return "입력한 조건을 다시 확인해 주세요.";
        }
        if (c.getAge() != null && (c.getAge() < 0 || c.getAge() > 120)) {
            return "나이를 다시 확인해 주세요.";
        }
        if (c.getAnnualIncome() != null && c.getAnnualIncome() < 0) {
            return "연소득은 0 이상으로 입력해 주세요.";
        }
        return null;
    }

    // 조건 수정 칸과 선택지 목록을 화면에 넘김
    private void addConditionForm(Model model, ConditionRequestDTO conditions) {

        model.addAttribute("conditions", conditions);

        model.addAttribute("regionOptions", Region.values());
        model.addAttribute("schoolOptions", userChoices(SchoolRequirement.values()));
        model.addAttribute("jobOptions", userChoices(JobRequirement.values()));
        model.addAttribute("marriageOptions", userChoices(MarriageRequirement.values()));
        model.addAttribute("majorOptions", userChoices(MajorRequirement.values()));
        model.addAttribute("specialTargetOptions", userChoices(SpecialTargetRequirement.values()));

        // AI가 목록에 없는 분야를 골랐어도 선택지에 남겨서, 다시 추천할 때 사라지지 않게 함
        List<String> categories = new ArrayList<>(CATEGORIES);
        String current = conditions.getCategory();
        if (current != null && !current.isBlank() && !categories.contains(current)) {
            categories.add(current);
        }
        model.addAttribute("categoryOptions", categories);
    }

    // 사용자가 고를 수 있는 값만 (알 수 없음·제한없음 제외 — AI 프롬프트와 같은 기준)
    private <E extends Enum<E>> List<E> userChoices(E[] values) {

        return Arrays.stream(values)
                .filter(v -> !v.name().equals("UNKNOWN") && !v.name().equals("NO_RESTRICTION"))
                .toList();
    }
}
