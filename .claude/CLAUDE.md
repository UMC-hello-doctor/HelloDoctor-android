# HelloDoctor-android 프로젝트 가이드

## 📌 프로젝트 개요

**HelloDoctor-android**는 의료 서비스 플랫폼의 Android 클라이언트입니다.

- **기술 스택**: Kotlin, Jetpack Compose / XML, Retrofit, Room, Hilt, Coroutines
- **아키텍처**: 클린 아키텍처 (3계층: Presentation, Domain, Data)
- **구조**: `app` (DI/Entry) + `core` (공용) + `feature` (비즈니스 기능)

## 🏗 주요 규칙

프로젝트의 아키텍처, 코딩 컨벤션, 커밋 규칙, 테스트 규칙은 다음을 참고하세요:

### 규칙 목록

| 규칙 | 설명 |
|-----|------|
| [architecture.md](rules/architecture.md) | 클린 아키텍처 및 계층 구조 |
| [convention.md](rules/convention.md) | 코딩 컨벤션 및 네이밍 규칙 |
| [commit.md](rules/commit.md) | Git 커밋 메시지 규칙 |
| [testing.md](rules/testing.md) | 유닛 테스트 및 테스트 전략 |

## 📂 프로젝트 구조

```
umc.hellodoctor
├─ app/                    # 앱 엔트리, DI, 네비게이션
│  ├─ HelloDoctorApp      # Application 클래스
│  ├─ MainActivity        # 진입 Activity
│  └─ di/                 # DI 모듈 (AppModule, NetworkModule, AuthModule 등)
├─ core/                   # 공용 코드 (UI, Network, Utils)
│  ├─ design/            # 디자인 시스템 상수
│  ├─ model/             # 공용 모델 (ApiError, Result 등)
│  ├─ network/           # ApiClient, AuthInterceptor, NetworkResult
│  ├─ ui/                # 공용 UI 컴포넌트, 확장함수, 테마
│  └─ util/              # 유틸리티 (DateTimeUtil, Logger, ResourceProvider)
└─ feature/               # 비즈니스 기능
   ├─ auth/              # 인증 기능 (data, domain, presentation, ui)
   ├─ home/              # 홈 기능
   ├─ settings/          # 설정 기능
   └─ basicFolder/       # 템플릿 (새 기능 추가 시 복사)

```

## 🚀 Claude Code 커맨드

이 프로젝트의 `.claude/commands/` 및 `.claude/agents/` 디렉토리에 정의된 커맨드를 사용할 수 있습니다:

| 커맨드 | 용도 |
|--------|------|
| `/feature` | 새 기능 구현 (planner → coder → refactorer → reviewer) |
| `/bug` | 버그 수정 (analyzer → coder → reviewer) |
| `/api` | Retrofit 엔드포인트 스캐폴딩 |
| `/test` | 유닛 테스트 생성 |
| `/issue` | GitHub 이슈 생성 |
| `/pr` | PR 자동 생성 |
| `/workflow` | CI 워크플로우 관리 |

자세한 내용은 [`.claude/commands/README.md`](commands/README.md)를 참고하세요.


---

**최종 업데이트**: 2026-03-30
**담당**: UMC HelloDoctor 팀
