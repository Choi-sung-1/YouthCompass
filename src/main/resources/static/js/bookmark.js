// =============================================================
// 찜 버튼 비동기 토글 (fragments/bookmark.html 의 form)
//  - 새로고침 없이 POST /bookmarks/{id} (X-Requested-With: fetch → JSON 응답)
//  - 서버가 알려준 찜 상태대로 버튼 글자·색만 바꿈 (스크롤·필터 유지)
//  - 사용: 페이지 맨 아래 <script th:src="@{/js/bookmark.js}"></script>
// =============================================================
(function () {

    function render(btn, bookmarked) {
        btn.classList.toggle("btn-primary", bookmarked);
        btn.textContent = bookmarked ? "♥ 찜 해제" : "♡ 찜하기";
    }

    document.querySelectorAll('form[action*="/bookmarks/"]').forEach(function (form) {
        form.addEventListener("submit", function (event) {
            event.preventDefault();
            var btn = form.querySelector("button");
            btn.disabled = true;

            fetch(form.action, { method: "POST", headers: { "X-Requested-With": "fetch" } })
                .then(function (res) {
                    // 로그인이 풀렸으면 로그인 화면(HTML)으로 리다이렉트됨 → 실패로 처리
                    if (!res.ok || res.url.includes("/member/login")) {
                        throw new Error("찜 실패");
                    }
                    return res.json();
                })
                .then(function (data) {
                    render(btn, data.bookmarked);
                })
                .catch(function () {
                    alert("찜 처리에 실패했어요. 다시 로그인한 뒤 시도해 주세요.");
                })
                .finally(function () {
                    btn.disabled = false;
                });
        });
    });
})();
