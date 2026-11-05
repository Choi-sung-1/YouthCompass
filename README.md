# YouthCompass

내 상황을 한 문장으로 적으면, 받을 수 있는 청년 정책을 AI가 골라 주는 웹 서비스입니다.

> 예) "수원 사는 25살 대학생이에요. 연봉은 2400만원이고 월세 지원을 찾고 있어요."
> → 나이·지역·학력·소득·찾는 지원을 읽어 내고, 자격이 되는 정책 중 도움이 될 순서로 TOP 10을 추천

정책 데이터는 [온통청년](https://www.youthcenter.go.kr) 청년정책 API에서 받아 옵니다.

---

## 주요 기능

### AI 정책 추천 (`/policy/recommend`)
- **문장으로 조건 입력:** OpenAI가 문장에서 나이·사는 곳(시·도, 시·군)·학력·취업 상태·혼인·연소득·전공·지원 대상·찾는 분야를 뽑아냄
- **회원정보로 빈칸 채우기:** 문장에 없는 나이·지역은 회원정보에서, 학력·소득 등은 마이페이지 "내 조건 프로필"에서 자동으로 채움
- **자격 판정 → AI 맞춤 순위:** 자격 조건을 규칙으로 판정한 뒤, 통과한 정책 중 AI가 내 상황에 맞는 순서로 TOP 10과 추천 이유·기대 효과를 작성
- **조건 직접 고치기:** AI가 잘못 이해한 조건을 고쳐서 다시 추천
- **확인 필요 조건 다시 확인:** "소득은 확인 필요"처럼 판단이 안 된 조건에 답하면 그 정책만 다시 판정
- **조건 하나 차이로 놓친 정책:** 한 가지 조건만 안 맞아 빠진 정책과 그 이유
- 결과는 한 줄에 3개 카드, 요약(바로 신청 가능 / 7일 안에 마감 / 확인 필요), 신청 준비(신청 방법·제출서류 체크리스트), 찜, 마감일 캘린더 추가
- AI가 실패하면 안내 문구를 보여 주고, 조건 수정 추천은 AI 없이 조건 일치 순서로 보여 줌

### 정책 둘러보기 (`/policies`)
- 분야·지역·검색어 필터, 신청 가능만 보기, 정렬(최신 등록순 / 마감 임박순 / 최근 모집 시작순), 페이지 이동
- 정책 상세: 지원 내용·신청 방법·제출 서류, 로그인하면 **"나에게 맞을까?"** 조건별 충족 / 미충족 / 확인 필요 판정
- 마감일 캘린더 파일(.ics) 내려받기 (하루 전 알림 포함)

### 마이페이지 (`/mypage`)
- 내 활동 요약: 찜한 정책 수, 7일 안에 마감되는 찜, 추천 기록 수
- 내 조건 프로필: 시·군, 학력, 취업 상태, 혼인, 전공, 연소득 (AI 추천·상세 판정에 자동 사용)
- 찜 목록 (`/mypage/bookmarks`)
- 추천 기록 (`/mypage/history`): 그때 입력한 문장·조건·TOP 10을 AI 재호출 없이 다시 보기, 지금 정책 정보로 자격 다시 확인

### 관리자
- 상단 바 **"정책 동기화"** 버튼 (`ROLE_ADMIN`만): 온통청년 API에서 정책 전체를 다시 받고, 새로 생기거나 바뀐 정책의 제출서류를 AI가 체크리스트로 정리
- API에서 사라진 정책은 자동으로 비활성화 (모두 정상으로 받았을 때만)

---

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 언어 / 런타임 | Java 17 |
| 프레임워크 | Spring Boot 4, Spring MVC, Spring Security (폼 로그인) |
| 화면 | Thymeleaf, 순수 CSS·JavaScript |
| DB | H2 (파일 DB, MySQL 모드), MyBatis |
| AI | Spring AI + OpenAI (`gpt-5-mini`) |
| 외부 데이터 | 온통청년 청년정책 API |
| 빌드 | Gradle |

---

## 실행 방법

### 1. 설정 파일 만들기
설정 파일 두 개는 비밀번호·API 키가 들어가서 Git에 올리지 않습니다 (`.gitignore`). 예시 파일을 복사해서 만듭니다.

```bash
cd src/main/resources
cp application-example.properties application.properties
cp application-secret-example.properties application-secret.properties
```

`application-secret.properties`에 키를 채웁니다.

```properties
# 온통청년 청년정책 API
policy.api.base-url=https://www.youthcenter.go.kr
policy.api.key=발급받은_온통청년_API_키

# OpenAI
spring.ai.openai.api-key=발급받은_OpenAI_API_키
```

DB 파일 위치는 `application.properties`의 `spring.datasource.url`에서 바꿀 수 있습니다 (기본값: 프로젝트 폴더의 `db/`).

### 2. 처음 한 번: 테이블 만들기
`application.properties`에서 아래 값을 `always`로 바꾸고 한 번 실행한 뒤, 다시 `never`로 돌려 둡니다.
(`always`로 두면 실행할 때마다 `schema.sql`이 다시 돌아갑니다.)

```properties
spring.sql.init.mode=always
```

### 3. 실행

```bash
./gradlew bootRun
```

→ http://localhost:8080

### 4. 처음 한 번: 정책 데이터 받기
1. 회원가입
2. 서버를 끄고, DB 도구(IntelliJ Database 창, DBeaver 등)로 `application.properties`의 `spring.datasource.url` 주소(사용자 `sa`, 비밀번호 없음)에 접속해서 내 계정을 관리자로 바꾸기
   ```sql
   UPDATE TBL_MEMBER SET MEMBER_ROLE = 'ROLE_ADMIN' WHERE MEMBER_LOGIN_ID = '내아이디';
   ```
   (H2 파일 DB는 한 번에 한 프로그램만 열 수 있어서 서버를 끄고 접속합니다. `/h2-console`은 보안 설정 때문에 열리지 않습니다.)
3. 서버를 다시 켜고 로그인하면 상단 바에 생기는 **"정책 동기화"** 버튼 누르기 (정책 약 4,000개, 1분 안팎)

---

## 테스트

```bash
./gradlew test     # 평소 테스트 (AI 호출 없음)
./gradlew aiTest   # 실제 OpenAI를 호출하는 테스트 (@Tag("ai"), 비용 발생)
```

---

## 프로젝트 구조

```
src/main/java/com/app/YouthCompass
├── config          보안 설정(SecurityConfig), 모든 화면 공통 모델(로그인 정보)
├── home            홈 화면 (정책 수, 마감 임박, 최근 모집 시작)
├── member          회원가입·로그인 (Spring Security)
├── profile         마이페이지, 내 조건 프로필
├── bookmark        찜
├── history         추천 기록 저장·조회·삭제
├── openAI          문장 → 조건 추출 (OpenAI)
└── policy
    ├── domain        정책·자격 조건 모델 (지역, 학력, 취업, 소득 등)
    ├── eligibility   자격 판정, 놓친 정책 찾기, 확인 필요 조건 다시 판정
    ├── recommendation  추천 흐름, AI 맞춤 순위·추천 이유, 회원정보로 빈칸 채우기
    ├── external      온통청년 API 호출·변환·동기화, 제출서류 AI 정리
    └── dto           화면용 데이터

src/main/resources
├── templates       Thymeleaf 화면 (fragments/ 는 여러 화면이 같이 쓰는 조각)
├── static          css/common.css(공통), css/policy-card.css(추천 카드), js/
├── mapper          MyBatis SQL
├── schema.sql      테이블 정의
└── sigungu.csv     시·군 코드 목록
```

### 화면 주소

| 주소 | 화면 | 로그인 |
|---|---|---|
| `/` | 홈 | - |
| `/policies` | 정책 목록 | - |
| `/policies/{id}` | 정책 상세 | - (로그인하면 조건 판정) |
| `/policy/recommend` | AI 정책 추천 | 필요 |
| `/mypage` | 마이페이지 | 필요 |
| `/mypage/bookmarks` | 찜 목록 | 필요 |
| `/mypage/history` | 추천 기록 | 필요 |
| `/member/join`, `/member/login` | 회원가입, 로그인 | - |

---

## 참고

- 공공데이터 원문의 빈 값·`해당없음`·`-`는 화면에 보여 주지 않고, `www.xxx.kr`처럼 `http`가 없는 링크는 `https://`를 붙여서 씁니다.
- 날짜(D-day, 마감 여부)는 한국 시간 기준입니다. 서버는 한국 시간대(`Asia/Seoul`)로 실행하는 것을 권장합니다.
