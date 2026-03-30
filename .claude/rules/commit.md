# Git 커밋 규칙

## 개요

HelloDoctor-android는 **Conventional Commits** 규칙을 따릅니다.

---

## 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 예시

```
feat(auth): 구글 로그인 기능 추가

사용자가 구글 계정으로 빠르게 가입/로그인할 수 있도록 구글 로그인 UI와 서버 통신 로직을 구현했습니다.

- GoogleSignInClient 통합
- 토큰 검증 및 사용자 프로필 매핑
- 에러 핸들링 (취소, 실패 케이스)

Closes #42
```

---

## 1. Type (필수)

커밋의 종류를 명시합니다.

| Type | 설명 | 예시 |
|------|------|------|
| **feat** | 새로운 기능 추가 | `feat(auth): 로그인 기능` |
| **fix** | 버그 수정 | `fix(home): 스크롤 시 크래시` |
| **refactor** | 코드 리팩토링 (기능 변화 없음) | `refactor(auth): 로그인 로직 간단히` |
| **style** | 포맷팅, 세미콜론, 들여쓰기 등 (코드 의미 없음) | `style: 들여쓰기 정리` |
| **test** | 테스트 추가/수정 | `test(auth): LoginViewModel 테스트` |
| **docs** | 문서 수정 | `docs: README 업데이트` |
| **chore** | 빌드, 의존성, CI/CD, 번역 등 | `chore: gradle 업그레이드` |
| **ci** | CI/CD 설정 수정 | `ci: GitHub Actions 추가` |
| **perf** | 성능 개선 | `perf(home): 이미지 로딩 최적화` |

**예**: `feat(auth): 구글 로그인 기능`, `fix(home): NPE 버그 수정` (type 필수, 소문자)

---

## 2. Scope (선택)

계층이나 모듈 이름: `auth`, `home`, `core`, `network`, `ui` 등. Scope가 없으면 전체 프로젝트. 클래스명이나 이슈 번호는 사용 금지.

---

## 3. Subject (필수)

50글자 이내의 간단한 설명. **명령형** 사용, 첫 글자 대문자, 마침표/느낌표 금지.

### Subject 예시

```bash
feat(auth): 구글 로그인 기능
feat(home): 약국 목록 스크롤 성능 개선
fix(auth): 토큰 만료 시 자동 재시도
fix(home): NPE 크래시 버그
refactor(core): 네트워크 클라이언트 단순화
docs: README 업데이트
chore: gradle 의존성 업그레이드
test(auth): LoginViewModel 테스트 추가
```

---

## 4. Body (선택, 권장)

더 자세한 설명입니다.

### Body 작성 규칙

1. **Subject와 한 줄 빈 칸**으로 분리
2. **무엇을 했는지**와 **왜 했는지** 설명
3. **각 항목은 줄을 분리**하여 작성

### Body 예시

```
feat(auth): 구글 로그인 기능 추가

사용자가 구글 계정으로 가입/로그인할 수 있습니다.

변경사항:
- GoogleSignInClient 통합
- 토큰 검증 및 사용자 프로필 매핑
- 자동 로그인 기능 추가
- 에러 핸들링 (취소, 실패, 네트워크 오류)

기술적 세부사항:
- AuthRemoteDataSource에 googleLogin() 추가
- AuthRepositoryImpl에 구글 토큰 검증 로직 구현
- LoginViewModel에 구글 로그인 이벤트 처리

왜 이 작업이 필요했는가:
- 사용자 가입 절차 간소화 (이메일 가입보다 40% 빠름)
- 경쟁사 대비 사용자 경험 향상
```

### ✅ 좋은 Body

```
fix(home): 약국 목록 NPE 크래시

약국 목록을 스크롤할 때 객체가 null이면 NullPointerException 발생.

근본 원인:
- PharmacyItem이 null인 상태에서 name 접근
- Repository에서 null 체크 없이 반환

해결:
- PharmacyItem 필드에 기본값 설정
- Repository에서 null 필터링 추가
- UI에서 safe navigation 적용
```

### ❌ 좋지 않은 Body

```
- 버그 수정
- 코드 정리
- 개선
```

---

## 5. Footer (선택)

이슈 참조나 Breaking Changes를 명시합니다.

### Breaking Changes

```
feat(auth): 로그인 API 변경

BREAKING CHANGE: 기존 AuthRequest 형식이 변경되었습니다.
- 기존: { "email": "...", "password": "..." }
- 변경: { "username": "...", "password": "...", "mfaToken": "..." }
```

### 이슈 참조

```
feat(auth): 구글 로그인 추가

Closes #42
Related #41, #43
```

### 다중 Footer

```
fix(auth): 토큰 검증 버그

관련 이슈 해결:
Closes #42
Closes #43

Breaking Change:
토큰 형식이 JWT에서 OAuth2로 변경되었습니다.

Reviewed-by: @john.doe
Co-Authored-By: @jane.smith <jane@example.com>
```

---


---

## 체크리스트

- Type이 올바른가? (feat, fix, refactor 등)
- Subject가 50글자 이내인가?
- Subject가 명령형인가?
- Body가 필요하면 작성했는가?
- 관련 이슈를 footer에 참조했는가?

