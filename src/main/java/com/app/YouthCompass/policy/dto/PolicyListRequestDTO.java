package com.app.YouthCompass.policy.dto;

import com.app.YouthCompass.policy.domain.eligibility.Region;
import lombok.Data;

// 정책 목록 검색 조건 (GET /policies 쿼리 파라미터가 그대로 바인딩됨)
@Data
public class PolicyListRequestDTO {

    public static final int PAGE_SIZE = 12;

    private String category;          // 대분류 한글 (PolicyCategories.ALL 중 하나, 빈 값 = 전체)
    private Region region;            // 시·도 (빈 값 = 전체)
    private String keyword;           // 정책명·키워드·설명 검색
    private boolean openOnly = true;  // 신청 가능한 것만 (첫 화면 기본값)
    private String sort;              // latest(기본, 최신 등록순) / deadline(마감 임박순) / recent(최근 모집 시작순)
    private int page = 1;

    // 정렬 값 정규화 (알 수 없는 값은 기본 정렬)
    public String getSort() {
        if ("deadline".equals(sort) || "recent".equals(sort)) {
            return sort;
        }
        return "latest";
    }

    // 이상한 페이지 번호 방어
    public int getPage() {
        return Math.max(page, 1);
    }

    // MyBatis에서 #{offset}, #{size}로 사용
    public int getOffset() {
        return (getPage() - 1) * PAGE_SIZE;
    }

    public int getSize() {
        return PAGE_SIZE;
    }

    // 빈 문자열은 조건 없음으로
    public String getCategory() {
        return category == null || category.isBlank() ? null : category.trim();
    }

    public String getKeyword() {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}