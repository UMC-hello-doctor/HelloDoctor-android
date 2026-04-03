# WIP: 코드 품질 개선 작업 (임시 저장)

**상태**: 🟡 **진행 중 (WIP)**  
**마지막 업데이트**: 2026-04-03  
**담당**: 심규석  
**Branch**: feat/62-composeview-integration

---

## 📊 현재 진행 상황

### 완료된 작업 ✅
- ✅ 패키지명 변경: `design_system` → `designsystem`
- ✅ 파일명 변경: `Color.kt` → `HelloDoctorColors.kt`
- ✅ ktlintFormat 자동 실행 (스타일 규칙)
- ✅ 105개 에러 → 41개 에러 (61% 감소)

### 검사 결과 (최신)
```
실행 명령: ./gradlew ktlintCheck detekt --no-daemon --parallel
결과: FAIL (41개 에러)

에러 분포:
- @Preview 함수명: 7개
- 인라인 주석 위치: 25개
- KDoc/주석 간격: 9개
```

---

## 🎯 남은 작업 (3단계)

### Phase 1: @Preview 함수명 변경 (10분)
**상태**: ⏳ 대기 중

7개 함수를 camelCase로 변경:

| 파일 | 라인 | 현재 | 변경 후 |
|------|------|------|--------|
| PrimaryButton.kt | 39 | `PrimaryButton` | `primaryButton` |
| PrimaryButton.kt | 75 | `PrimaryButtonPreview` | `primaryButtonPreview` |
| PrimaryButton.kt | 86 | `PrimaryButtonDisabledPreview` | `primaryButtonDisabledPreview` |
| SecondaryButton.kt | 38 | `SecondaryButton` | `secondaryButton` |
| SecondaryButton.kt | 74 | `SecondaryButtonPreview` | `secondaryButtonPreview` |
| SecondaryButton.kt | 85 | `SecondaryButtonDisabledPreview` | `secondaryButtonDisabledPreview` |
| HelloDoctorTheme.kt | 82 | `HelloDoctorThemePreview` | `helloDoctorThemePreview` |

### Phase 2: 인라인 주석 처리 (15분)
**상태**: ⏳ 대기 중

**파일**: `theme/HelloDoctorTheme.kt` (Lines 17-41)  
**문제**: 함수 인자 내 인라인 주석이 같은 줄에 위치  
**해결**: 주석 제거 또는 별도 줄로 분리

```kotlin
// 현재 (문제)
val colorScheme = lightColorScheme(
    primary = HelloDoctorColors.Primary, // Primary color
    onPrimary = HelloDoctorColors.OnPrimary, // On Primary text
)

// 변경 후 (권장: 주석 제거)
val colorScheme = lightColorScheme(
    primary = HelloDoctorColors.Primary,
    onPrimary = HelloDoctorColors.OnPrimary,
)
```

**영향**: 25개 주석

### Phase 3: KDoc/주석 간격 추가 (10분)
**상태**: ⏳ 대기 중

**문제**: EOL 주석 바로 다음에 KDoc 시작  
**해결**: 빈 줄 추가

```kotlin
// 현재 (문제)
// Color palette
/**
 * Description
 */

// 변경 후 (해결)
// Color palette

/**
 * Description
 */
```

**영향 파일**:
- `token/HelloDoctorColors.kt`: Lines 18, 31, 43, 55, 67, 74, 93, 100 (8개)
- `token/HelloDoctorDimensions.kt`: Line 57 (1개)

---

## 📋 체크리스트

### Phase 1: @Preview 함수명
- [ ] PrimaryButton.kt line 39
- [ ] PrimaryButton.kt line 75
- [ ] PrimaryButton.kt line 86
- [ ] SecondaryButton.kt line 38
- [ ] SecondaryButton.kt line 74
- [ ] SecondaryButton.kt line 85
- [ ] HelloDoctorTheme.kt line 82

### Phase 2: 인라인 주석
- [ ] HelloDoctorTheme.kt lines 17-41 검토 및 정리

### Phase 3: KDoc/주석 간격
- [ ] HelloDoctorColors.kt 8개 위치
- [ ] HelloDoctorDimensions.kt 1개 위치

### 최종 확인
- [ ] `./gradlew ktlintCheck detekt --no-daemon --parallel` 실행
- [ ] 모든 에러 해결 확인
- [ ] PR 업데이트

---

## 📈 예상 결과

**Phase 1-3 완료 후**:
```
현재: 41개 에러
↓
목표: 0개 에러 (ktlintCheck & detekt PASS)
```

---

## 📚 참고 문서

- **현재 계획**: `docs/code-quality-improvement-status.md`
- **초기 분석**: `docs/code-quality-improvement-plan.md`
- **상세 기술**: `docs/code-quality-errors-detail.md`
- **메모리**: `memory/code-quality-status.md`

---

## 🔗 관련 정보

- **PR**: [UMC-hello-doctor/HelloDoctor-android/pull/74](https://github.com/UMC-hello-doctor/HelloDoctor-android/pull/74)
- **Branch**: `feat/62-composeview-integration`
- **Base**: `feat/59-xml-to-compose-migration`

---

## ⏱ 예상 소요 시간

| 단계 | 작업 | 시간 |
|-----|------|------|
| Phase 1 | @Preview 함수명 변경 | 10분 |
| Phase 2 | 인라인 주석 정리 | 15분 |
| Phase 3 | KDoc/주석 간격 추가 | 10분 |
| 최종 | 검사 & 커밋 | 5분 |
| **합계** | | **40분** |

---

**다음 세션**: 위의 Phase 1-3을 순서대로 수행하면 ktlintCheck & detekt 통과 가능

---

마지막 업데이트: 2026-04-03 14:30
