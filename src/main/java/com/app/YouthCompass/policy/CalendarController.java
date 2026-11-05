package com.app.YouthCompass.policy;

import com.app.YouthCompass.policy.domain.model.PolicyText;
import com.app.YouthCompass.policy.domain.model.PolicyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// 정책 신청 마감일을 캘린더 파일(.ics)로 내려줌 → 폰·구글·애플 캘린더에서 열면 일정 추가
@Controller
@RequiredArgsConstructor
public class CalendarController {

    private static final DateTimeFormatter ICS_DATE = DateTimeFormatter.BASIC_ISO_DATE;   // 20261130
    private static final DateTimeFormatter ICS_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final PolicyMapper policyMapper;   // A 담당 — findPolicyById 호출만

    // GET /policy/{policyId}/calendar.ics — 마감일 하루짜리 일정 + 하루 전 알림
    @GetMapping("/policy/{policyId}/calendar.ics")
    public ResponseEntity<byte[]> calendar(@PathVariable Long policyId) {

        PolicyVO policy = policyMapper.findPolicyById(policyId);
        if (policy == null || policy.getPolicyApplyEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "마감일이 있는 정책이 아니에요.");
        }

        LocalDate end = policy.getPolicyApplyEndDate();
        String name = PolicyText.text(policy.getPolicyName());
        if (name == null) {
            name = "청년 정책";
        }
        String url = PolicyText.applyUrl(policy);

        // iCalendar 형식: 줄바꿈은 반드시 CRLF(\r\n)
        String ics = String.join("\r\n",
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//YouthCompass//Policy Deadline//KO",
                "CALSCALE:GREGORIAN",
                "BEGIN:VEVENT",
                "UID:policy-" + policyId + "@youthcompass",
                "DTSTAMP:" + ZonedDateTime.now(ZoneOffset.UTC).format(ICS_STAMP),
                "DTSTART;VALUE=DATE:" + end.format(ICS_DATE),
                "DTEND;VALUE=DATE:" + end.plusDays(1).format(ICS_DATE),
                "SUMMARY:" + escape("[신청 마감] " + name),
                "DESCRIPTION:" + escape(url == null ? name : name + " 신청: " + url),
                "BEGIN:VALARM",
                "TRIGGER:-P1D",
                "ACTION:DISPLAY",
                "DESCRIPTION:" + escape("내일 신청 마감: " + name),
                "END:VALARM",
                "END:VEVENT",
                "END:VCALENDAR",
                "");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("policy-" + policyId + ".ics").build().toString())
                .body(ics.getBytes(StandardCharsets.UTF_8));
    }

    // iCalendar 규칙: \ ; , 앞에 \, 줄바꿈은 \n 으로
    private String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}