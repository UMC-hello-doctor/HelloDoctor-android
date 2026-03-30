# workflows

PR 생성부터 머지까지 전 과정을 자동화하는 GitHub Actions 워크플로우 모음입니다.

## 워크플로우 목록

| 파일 | 트리거 | 역할 |
|---|---|---|
| `code-style-check.yml` | PR 생성/업데이트 | Kotlin 코드 스타일 검사 |
| `test-coverage.yml` | PR 생성/업데이트 | 테스트 커버리지 검증 |
| `pr-line-limit.yml` | PR 생성/업데이트/라벨 변경 | PR 변경 줄 수 제한 |
| `pr-title-labeler.yml` | PR 생성/수정 | PR 제목 접두사 기반 라벨 부착 |
| `pr-labeler.yml` | PR 이벤트 전체 | 파일 경로 기반 라벨 + 상태 라벨 관리 |
| `issue-labeler.yml` | 이슈 생성/수정 | 이슈 상태/키워드 기반 라벨 부착 |
| `labels-sync.yml` | develop 브랜치 push / 수동 | GitHub 라벨 목록 동기화 |
| `pr-merge-status.yml` | PR 머지 완료 | 연결 이슈 상태 라벨 완료 처리 |

---

## 상세 설명

### code-style-check.yml
- **트리거**: PR opened / synchronize
- **도구**: `ktlint` (포맷 검사) + `detekt` (정적 분석) 병렬 실행
- **환경**: JDK 17, Gradle 캐시 적용 (빌드 속도 최적화)
- **주의**: 두 검사 모두 실패해도 `--continue-on-error` 로 결과를 전부 출력

### test-coverage.yml
- **트리거**: PR opened / synchronize
- **도구**: JaCoCo (`jacocoTestCoverageVerification`)
- **임계값**: 커버리지 **80% 미만 시 실패**
- **산출물**: `build/reports/jacoco/` HTML 리포트를 Artifact로 업로드

### pr-line-limit.yml
- **트리거**: PR opened / synchronize / labeled
- **기준**: 추가 줄 수 기준 (삭제 줄 제외), 테스트/문서/XML 리소스 제외
- **임계값**:
  - 180줄 이상 → ⚠️ 경고 댓글 + 리뷰어 추가 권장
  - 200줄 초과 → 🚫 실패 + PR 분리 요청 댓글
- **예외**: `design` 라벨이 붙은 PR은 검사 스킵

### pr-title-labeler.yml
- **트리거**: PR opened / edited / synchronize
- **동작**: PR 제목의 접두사를 감지하여 타입 라벨 부착

| 접두사 | 부착 라벨 |
|---|---|
| `[Feature]` | `✨ feature` |
| `[Bug]` | `🐞 bug` |
| `[Refactor]` | `🔧 refactor` |
| `[Chore]` | `🧹 chore` |
| `[Design]` | `💡 design` |

### pr-labeler.yml
- **트리거**: PR opened / synchronize / reopened / edited / review_requested / review_request_removed
- **동작 1**: `.github/labels/labeler.yml` 기준으로 변경 파일 경로에 따라 레이어 라벨 자동 부착
- **동작 2**: 상태 라벨 자동 전환

| 이벤트 | 라벨 변경 |
|---|---|
| PR 오픈 / 재오픈 | `🏃 status: in progress` 부착 |
| 리뷰 요청 | `🔍 status: code review` 부착 |
| 리뷰 요청 해제 | `🔍 status: code review` 제거 |

### issue-labeler.yml
- **트리거**: 이슈 opened / edited
- **동작 1**: 모든 신규 이슈에 `📌 status: todo` 자동 부착
- **동작 2**: `.github/labels/issue-labeler.yml` 의 키워드 목록 기준으로 제목+본문 스캔 후 타입 라벨 부착

### labels-sync.yml
- **트리거**: `develop` 브랜치에 `.github/labels/labels.yml` 변경 push 또는 수동 실행
- **동작**: `crazy-max/ghaction-github-labeler` 를 사용해 `labels.yml` 정의 기준으로 GitHub 라벨을 동기화
- **주의**: `skip-delete: true` 설정으로 파일에 없는 기존 라벨은 삭제하지 않음

### pr-merge-status.yml
- **트리거**: PR closed (머지된 경우만 실행)
- **동작**: PR 본문에서 `#이슈번호` 패턴을 추출하여 연결된 이슈에 아래 처리 수행
  1. `🏃 status: in progress` 라벨 제거
  2. `✅ status: done` 라벨 부착

---

## 자동화 전체 흐름

```
이슈 생성
  └─ issue-labeler     → 📌 status: todo + 타입 라벨 부착

PR 생성 (제목: [Feature] xxx, 본문: #이슈번호)
  ├─ pr-title-labeler  → ✨ feature 라벨 부착
  ├─ pr-labeler        → 🏃 status: in progress + 레이어 라벨 부착
  ├─ code-style-check  → ktlint + detekt 검사
  ├─ test-coverage     → 커버리지 80% 검증
  └─ pr-line-limit     → 200줄 초과 시 실패

리뷰 요청
  └─ pr-labeler        → 🔍 status: code review 부착

PR 머지
  └─ pr-merge-status   → 연결 이슈에 ✅ status: done 부착
```
