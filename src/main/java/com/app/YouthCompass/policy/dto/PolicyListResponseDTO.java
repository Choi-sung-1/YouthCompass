package com.app.YouthCompass.policy.dto;

import lombok.Getter;

import java.util.List;

// 정책 목록 화면 데이터 (카드 목록 + 페이지 정보)
@Getter
public class PolicyListResponseDTO {

    private static final int PAGE_BLOCK = 5;   // 페이지 번호를 한 번에 5개씩

    private final List<PolicyListItemDTO> items;
    private final int totalCount;
    private final int page;
    private final int totalPages;
    private final int startPage;
    private final int endPage;

    public PolicyListResponseDTO(List<PolicyListItemDTO> items, int totalCount, int page, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        this.page = Math.min(page, totalPages);
        this.startPage = ((this.page - 1) / PAGE_BLOCK) * PAGE_BLOCK + 1;
        this.endPage = Math.min(startPage + PAGE_BLOCK - 1, totalPages);
    }

    public boolean hasPrev() {
        return startPage > 1;
    }

    public boolean hasNext() {
        return endPage < totalPages;
    }
}