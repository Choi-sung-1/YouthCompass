package com.app.YouthCompass.policy.dto;

import java.util.List;

// POST /policy/recheck 응답 (JSON)
public record RecheckResponseDTO(

        // ELIGIBLE(신청 가능) | NEED_CHECK(아직 확인 필요) | NOT_ELIGIBLE(신청 어려움)
        String status,

        // 맞은 조건 문장 (추천 카드와 같은 문장)
        List<String> reasons,

        // 아직 확인이 필요한 조건 문장
        List<String> unknownReasons,

        // NOT_ELIGIBLE일 때: 걸린 조건 이름과 "이러면 신청할 수 있어요" 안내
        String notMatchCondition,
        String notMatchHint,

        // 더 물어볼 질문 (답하면 풀 수 있는 확인 필요 조건만)
        List<RecheckQuestionDTO> questions
) {}