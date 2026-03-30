# 슬래시 커맨드 (스킬) 폴더

이 폴더에는 Claude Code의 **사용자 정의 슬래시 커맨드** 정의 파일이 있습니다.

## 사용법

`/커맨드명 인자` 형태로 호출합니다. 파일의 내용이 프롬프트로 확장되어 Claude에게 전달됩니다.

## 커맨드 목록

| 커맨드 | 용도 | 에이전트 체인 |
|--------|------|--------------|
| `/feature` | 새 기능 전체 구현 | planner → coder → refactorer → reviewer |
| `/bug` | 버그 수정 | analyzer → coder → reviewer |
| `/pr` | PR 자동 생성 | 단독 (git + gh 명령) |
| `/api` | Retrofit 엔드포인트 스캐폴딩 | 단독 (coder 패턴 참조) |
| `/test` | 유닛 테스트 자동 생성 | 단독 |

---

## 상세 설명

### `/feature` — 기능 구현 파이프라인

새 기능을 플래너 → 코더 → 리팩터 → 리뷰어 순으로 구현합니다.

```
/feature 약국 즐겨찾기. 추가/제거 가능하고 로컬 DB에 저장
```

---

### `/bug` — 버그 수정 파이프라인

버그 설명을 받아 원인 분석 → 최소 수정 → 회귀 리뷰를 수행합니다.

```
/bug 약국 목록 화면에서 앱이 크래시남. 스크롤 내리면 NPE 발생
```

---

### `/pr` — PR 자동 생성

현재 브랜치의 변경사항을 읽고 프로젝트 PR 템플릿에 맞게 자동으로 PR을 생성합니다.
- 200줄 초과 시 경고 후 확인 요청 (프로젝트 규칙)
- 이슈 번호를 인자로 전달하면 `Closes #번호` 자동 추가

```
/pr
/pr 42
```

---

### `/api` — API 엔드포인트 스캐폴딩

REST 엔드포인트 설명을 받아 클린 아키텍처 5개 파일을 한 번에 생성합니다:
Response Model → API Interface → Repository Interface → Repository Impl → Hilt Module

```
/api GET /hospitals/nearby?lat={lat}&lng={lng} 주변 병원 목록 반환
```

---

### `/test` — 유닛 테스트 생성

파일 경로 또는 클래스명을 받아 ViewModel / Repository / UseCase 유닛 테스트를 작성합니다.
Happy path, Error path, Edge case를 모두 커버합니다.

```
/test feature/drug/presentation/DrugViewModel.kt
/test UserInfoRepositoryImpl
```

---

## 커맨드 추가 방법

```markdown
---
name: 커맨드-이름
description: 커맨드 역할 한 줄 설명
---

슬래시 커맨드 실행 시 Claude에게 전달될 프롬프트...
```

> 에이전트 정의는 [`../agents/README.md`](../agents/README.md)를 참고하세요.
