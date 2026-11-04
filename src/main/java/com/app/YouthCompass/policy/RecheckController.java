package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.dto.RecheckRequestDTO;
import com.app.YouthCompass.policy.dto.RecheckResponseDTO;
import com.app.YouthCompass.policy.eligibility.RecheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// 추천 카드 다시 판정 API (A 담당, 약속 ④) — PolicyController와 분리
// 로그인 필요 (SecurityConfig의 anyRequest().authenticated())
@RestController
@RequiredArgsConstructor
public class RecheckController {

    private final RecheckService recheckService;

    // POST /policy/recheck (JSON) → 판정 결과 + 더 물어볼 질문
    @PostMapping("/policy/recheck")
    public RecheckResponseDTO recheck(@RequestBody RecheckRequestDTO request) {

        return recheckService.recheck(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "정책을 찾을 수 없습니다."));
    }
}