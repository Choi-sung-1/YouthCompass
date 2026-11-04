// =============================================================
// 확인 필요 조건 다시 판정 (A 담당, 약속 ④)
//  - 추천 카드 안 recheck 조각(.recheck)마다 동작
//  - 조건 수정 폼(#condition-form)의 지금 값 + 이 카드에서 답한 값으로 POST /policy/recheck
//  - 결과·질문은 이 조각 안에만 표시 (카드의 다른 부분, 조건 수정 폼은 건드리지 않음)
//  - 서버 문장은 textContent로만 넣음 (HTML로 해석하지 않음)
// =============================================================
(function () {

    // 조건 수정 폼에서 읽을 칸 (ConditionRequestDTO 필드 이름과 같음)
    var FIELDS = ["age", "region", "sigungu", "school", "job", "marriage", "annualIncome", "major"];
    var NUMBER_FIELDS = ["age", "annualIncome"];
    var NONE_VALUE = "__NONE__";

    var STATUS_TEXT = {
        ELIGIBLE: "입력한 조건으로는 신청할 수 있어요.",
        NEED_CHECK: "아직 확인이 필요한 조건이 있어요.",
        NOT_ELIGIBLE: "입력한 조건으로는 신청이 어려워요."
    };

    // 조건 수정 폼의 지금 값 (빈 칸 = null = 모름)
    function readForm() {
        var conditions = {};
        var form = document.getElementById("condition-form");
        if (!form) {
            return conditions;
        }

        FIELDS.forEach(function (name) {
            var el = form.elements[name];
            if (!el || typeof el.value !== "string") {
                return;
            }
            var value = el.value.trim();
            conditions[name] = value === "" ? null : value;
        });

        var targets = Array.prototype.map.call(
            form.querySelectorAll('input[name="specialTargets"]:checked'),
            function (input) { return input.value; });
        conditions.specialTargets = targets.length ? targets : null;

        return conditions;
    }

    // 이 조각 안에서 답한 값
    function readAnswers(box) {
        var answers = {};
        var noSpecialTarget = false;

        box.querySelectorAll("[data-field]").forEach(function (el) {
            var field = el.getAttribute("data-field");

            if (el.type === "checkbox") {
                if (!el.checked) {
                    return;
                }
                if (el.value === NONE_VALUE) {
                    noSpecialTarget = true;
                    return;
                }
                (answers[field] = answers[field] || []).push(el.value);
                return;
            }

            var value = el.value.trim();
            if (value !== "") {
                answers[field] = value;
            }
        });

        return { answers: answers, noSpecialTarget: noSpecialTarget };
    }

    function send(box, answered) {
        // 답은 카드별로 누적 (두 번째 질문에 답할 때 첫 번째 답이 사라지지 않게)
        box._answers = Object.assign(box._answers || {}, answered.answers);
        box._noSpecialTarget = Boolean(box._noSpecialTarget || answered.noSpecialTarget);

        var conditions = Object.assign(readForm(), box._answers);
        NUMBER_FIELDS.forEach(function (field) {
            if (conditions[field] != null) {
                var number = Number(conditions[field]);
                conditions[field] = isNaN(number) ? null : number;
            }
        });

        var body = box.querySelector(".recheck-body");
        body.replaceChildren(text("p", "확인하는 중…"));

        fetch("/policy/recheck", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                policyId: Number(box.getAttribute("data-policy-id")),
                conditions: conditions,
                noSpecialTarget: box._noSpecialTarget
            })
        })
            .then(function (res) {
                // 로그인이 풀리면 로그인 화면으로 리다이렉트됨 → 실패로 처리
                if (!res.ok || res.redirected) {
                    throw new Error("다시 판정 실패");
                }
                return res.json();
            })
            .then(function (data) {
                render(box, data);
            })
            .catch(function () {
                body.replaceChildren(text("p", "다시 확인하지 못했어요. 다시 로그인한 뒤 시도해 주세요."));
            });
    }

    function render(box, data) {
        var body = box.querySelector(".recheck-body");
        body.replaceChildren(text("p", STATUS_TEXT[data.status] || ""));

        if (data.status === "NOT_ELIGIBLE" && data.notMatchHint) {
            body.appendChild(text("p", data.notMatchCondition + ": " + data.notMatchHint));
        }
        if (data.unknownReasons && data.unknownReasons.length) {
            body.appendChild(list(data.unknownReasons));
        }

        if (!data.questions || !data.questions.length) {
            return;
        }

        data.questions.forEach(function (q) {
            body.appendChild(question(q));
        });

        var button = text("button", "답하고 다시 확인");
        button.type = "button";
        button.addEventListener("click", function () {
            send(box, readAnswers(box));
        });
        body.appendChild(button);
    }

    // 질문 하나 → 입력 칸 (data-field = 보낼 필드 이름)
    function question(q) {
        var wrap = document.createElement("div");
        wrap.appendChild(text("span", q.label + " "));

        if (q.type === "select") {
            var select = document.createElement("select");
            select.setAttribute("data-field", q.field);
            select.add(new Option("선택", ""));
            q.options.forEach(function (o) {
                select.add(new Option(o.label, o.value));
            });
            wrap.appendChild(select);

        } else if (q.type === "checkbox") {
            q.options.concat([{ value: NONE_VALUE, label: "해당 없음" }]).forEach(function (o) {
                var label = document.createElement("label");
                var input = document.createElement("input");
                input.type = "checkbox";
                input.value = o.value;
                input.setAttribute("data-field", q.field);
                label.appendChild(input);
                label.appendChild(document.createTextNode(" " + o.label + " "));
                wrap.appendChild(label);
            });

        } else {
            var input = document.createElement("input");
            input.type = q.type === "number" ? "number" : "text";
            if (q.type === "number") {
                input.min = "0";
            }
            input.setAttribute("data-field", q.field);
            wrap.appendChild(input);
        }
        return wrap;
    }

    function text(tag, value) {
        var el = document.createElement(tag);
        el.textContent = value;
        return el;
    }

    function list(items) {
        var ul = document.createElement("ul");
        items.forEach(function (item) {
            ul.appendChild(text("li", item));
        });
        return ul;
    }

    // 시작: 처음엔 지금 조건 그대로 한 번 판정해서 물어볼 질문을 받아옴
    document.querySelectorAll(".recheck").forEach(function (box) {
        var start = box.querySelector(".recheck-start");
        if (!start) {
            return;
        }
        start.addEventListener("click", function () {
            start.hidden = true;
            send(box, { answers: {}, noSpecialTarget: false });
        });
    });
})();