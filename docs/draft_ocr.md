# OCR 처방전 인식 기능 - 진행 상황 보고

**작성일**: 2026-04-02
**상태**: 80% 완료 (기본 기능 완료, 안정화 작업 진행 중)

---

## 이슈 현황 정리

### 이슈 #54: [Feature] ML Kit OCR 텍스트 인식 기능 구현

**상태**: ✅ **완료**

**완료된 작업**:
- ✅ ML Kit OCR 라이브러리 추가 (OcrRepositoryImpl.kt)
  - `com.google.mlkit:text-recognition-korean:16.0.1` 의존성 추가
  - `KoreanTextRecognizer` 초기화
  - `suspendCancellableCoroutine` 패턴으로 비동기 처리

- ✅ 이미지 → InputImage 변환
  - EXIF 메타데이터 읽기 (`ExifInterface`)
  - 회전 각도 감지 (0°, 90°, 180°, 270°)
  - `InputImage.fromBitmap(bitmap, rotationDegrees)` 생성

- ✅ OCR 텍스트 추출 로직 구현
  - `recognizeText(uri: Uri): Result<OcrResult>` 메서드
  - 성공/실패 콜백 처리
  - 기본 예외 처리 (IOException, RuntimeException)

**파일**: `app/src/main/java/com/umc/hellodoctor/feature/drug/data/repository/OcrRepositoryImpl.kt`

**남은 작업**: None (완료)

---

### 이슈 #55: [Feature] OCR 결과 파싱 및 처방전 데이터 구조화

**상태**: ✅ **완료**

**완료된 작업**:
- ✅ OCR 결과 텍스트 파싱
  - `PrescriptionTextParser.parse(rawText: String)` 메서드
  - 정규식 기반 약 이름 추출

- ✅ 약 이름 추출 (기본 패턴)
  - `MEDICINE_SUFFIX_REGEX`: 접미사 기반 (정|캡슐|시럽|...)
  - `NUMBERED_MEDICINE_REGEX`: 번호 목록 형식 (1. 약명, 2) 약명)
  - 최소 길이 필터링 (3글자 이상)

- ✅ 복약 정보 파싱
  - `frequencyHint`: 1일 복용 횟수 추출 ("1일 3회" → "3회/일")
  - `dosageHint`: 1회 용량 추출 ("1회 2정" → "1회 2정")
  - `durationDays`: 복용 기간 추출 ("7일간" → 7)

- ✅ 텍스트 정규화
  - 연속 공백 → 단일 공백 (줄바꿈 유지)
  - 숫자-한글 사이 공백 제거 ("1 약" → "1약")

**파일**: `app/src/main/java/com/umc/hellodoctor/feature/drug/data/ocr/PrescriptionTextParser.kt`

**데이터 구조**: `feature/drug/domain/model/OcrResult.kt`
```kotlin
data class OcrResult(
    val rawText: String,
    val recognizedMedicineNames: List<String>,
    val frequencyHint: String?,
    val dosageHint: String?,
    val durationDays: Int?
)
```

**남은 작업**:
- 고급 파싱 (좌표 기반 라인 재구성) - ⏳ 차기 작업

---

### 이슈 #56: [Feature] OCR 결과 UI 표시 및 ViewModel 연동

**상태**: ✅ **완료**

**완료된 작업**:
- ✅ ViewModel 상태 관리
  - `PrescriptionScanViewModel`: 카메라 촬영 및 OCR 상태 관리
  - `PrescriptionScanUiState`: Idle | Loading | Success | Error 상태
  - `resetToIdle()`: 재촬영 기능

- ✅ OCR 결과 UI 표시
  - `OcrResultFragment`: 인식된 약 이름 목록 표시
  - 삭제 기능 (RecyclerView)
  - 약 정보 상세 보기 가능

- ✅ 촬영 → OCR → 결과 흐름 연결
  - `PrescriptionScanFragment`: 카메라 프리뷰 + 촬영 버튼
  - `CameraController.takePhoto()` → `OcrRepositoryImpl.recognizeText()`
  - `PrescriptionScanViewModel` → `OcrResultFragment` 네비게이션

**파일들**:
- `feature/drug/presentation/PrescriptionScanFragment.kt`: 카메라 UI + 가이드 오버레이
- `feature/drug/presentation/OcrResultFragment.kt`: 결과 표시
- `feature/drug/presentation/PrescriptionScanViewModel.kt`: 상태 관리

**남은 작업**: None (완료)

---

### 이슈 #57: [Feature] OCR 예외 처리 및 기능 안정화

**상태**: 🟡 **70% 완료** (기본 예외 처리 완료, 심화 처리 필요)

**완료된 작업**:
- ✅ OCR 실패 처리
  - `OcrRepositoryImpl`에서 Exception catch (IOException, RuntimeException)
  - `Result.failure()` 로 실패 전달
  - `PrescriptionScanViewModel`에서 Error 상태 표시

- ✅ 이미지 처리 예외
  - `BitmapFactory.decodeFile()` 실패 감지
  - `InputImage.fromFilePath()` 예외 처리

- ✅ 메모리 관리
  - `bitmap.recycle()` 호출로 메모리 누수 방지 (이미지 전처리 후)

- ✅ 기본 권한 처리
  - 카메라 권한 거부 시 Toast + 뒤로가기
  - `onPermissionDenied` 콜백 구현

**남은 작업** (⏳ 차기 구현):

1. **타임아웃 처리**
   - OCR 처리 시간이 길 경우 타임아웃 설정 필요
   - `withTimeoutOrNull()` 로 5초 제한 추천

2. **저화질 이미지 처리**
   - 이미지 전처리 완료 (그레이스케일 + 대비 강화)
   - 추가 처리: 노이즈 제거, 회전 각도 자동 감지

3. **사용자 안내 개선**
   - 실패 메시지 구체화 (파일 읽기 실패 vs OCR 실패 vs 타임아웃)
   - "다시 시도" 버튼 활성화

4. **성능 최적화**
   - 대용량 이미지 다운스케일 (OCR 입력 전)
   - 병렬 처리 (이미지 로드 + 전처리 동시화)

5. **로깅 및 모니터링**
   - OCR 소요 시간 기록
   - 실패율 통계 (향후 분석용)

**파일들**:
- `feature/drug/data/repository/OcrRepositoryImpl.kt`: 예외 처리
- `feature/drug/presentation/PrescriptionScanViewModel.kt`: 에러 상태 관리
- `feature/drug/presentation/PrescriptionScanFragment.kt`: 에러 UI 표시

---

## 📊 전체 진행 상황 요약

| 이슈 | 제목 | 상태 | 완료율 | 비고 |
|---|---|---|---|---|
| #54 | ML Kit OCR 텍스트 인식 | ✅ 완료 | 100% | EXIF 회전 처리 포함 |
| #55 | OCR 파싱 & 데이터 구조화 | ✅ 완료 | 100% | 정규식 보강됨 |
| #56 | OCR 결과 UI 표시 | ✅ 완료 | 100% | CameraX UI 포함 |
| #57 | OCR 예외 처리 & 안정화 | 🟡 부분완료 | 70% | 기본 처리 완료, 심화 필요 |

**전체**: 🟡 **80% 완료**

---

## 🎯 CameraX UI 추가 개선사항

PR #65에서 다음도 함께 구현됨:

### 카메라 기능 고도화
- ✅ **앱 내 카메라 프리뷰**: CameraX `Preview + ImageCapture` 사용
- ✅ **촬영 가이드 오버레이**: 처방전 크기 가이드 프레임 (너비 90%, A4 비율)
- ✅ **촬영 버튼**: 하단 중앙 72dp 원형 버튼
- ✅ **권한 처리**: 카메라 권한 거부 시 사용자 안내

### OCR 정확도 개선
- ✅ **EXIF 회전 처리**: 기기 회전 상태 무관하게 정상 방향 인식
- ✅ **이미지 전처리**: 그레이스케일 변환 + 대비 강화
  - 포화도: 0 (흑백)
  - 대비: scale=1.5, translate=-50
  - 효과: 밝기 불균일한 이미지에서도 글자 인식 향상

---

## ⏳ 차기 작업 (이슈 #57 심화)

### Phase 1: 안정성 강화 (1-2주)

```
1. 타임아웃 처리
   └─ withTimeoutOrNull(5000L) 적용
   └─ 사용자 안내 "분석 중단" UI

2. 사용자 안내 개선
   └─ 에러 타입별 메시지 (파일 읽기 실패 vs OCR 실패)
   └─ 재시도 로직 추가

3. 로깅 추가
   └─ OCR 소요 시간 기록
   └─ 실패 원인 분류 (파일 vs OCR vs 타임아웃)
```

### Phase 2: 성능 최적화 (2-3주)

```
1. 이미지 다운스케일
   └─ OCR 입력 전 너비 2048px 제한

2. 병렬 처리
   └─ 이미지 로드 + 전처리 async 병렬화

3. 캐싱 (선택)
   └─ OCR 결과 메모리 캐시 (사용자 재촬영 시)
```

### Phase 3: OCR 정확도 추가 개선 (3-4주)

```
1. 고급 파싱
   └─ 좌표 기반 라인 재구성 (현재: 라인 순서 미보장)
   └─ 테이블 형식 약 정보 추출

2. 모델 선택 (선택)
   └─ ML Kit 기본 vs Google Cloud Vision API
   └─ 정확도 vs 비용 트레이드오프

3. 사용자 피드백 수집
   └─ 인식 실패 로깅 (사용자 동의 시)
   └─ 정확도 개선용 데이터 수집
```

---

## 🔗 관련 파일 및 문서

| 파일 | 설명 |
|---|---|
| `docs/OcrPlan.md` | CameraX 및 OCR 전체 설계 문서 |
| `core/camera/CameraController.kt` | CameraX 래퍼 (Preview + ImageCapture) |
| `feature/drug/data/repository/OcrRepositoryImpl.kt` | OCR 로직 (ML Kit + 이미지 전처리) |
| `feature/drug/data/ocr/PrescriptionTextParser.kt` | 텍스트 파싱 (정규식 기반) |
| `feature/drug/presentation/PrescriptionScanFragment.kt` | 카메라 UI (Compose + CameraX) |
| `feature/drug/presentation/OcrResultFragment.kt` | 결과 표시 UI |
| `feature/drug/domain/model/OcrResult.kt` | 파싱 결과 데이터 클래스 |

---

## 📝 PR 현황

- **PR #65**: CameraX 기반 처방전 촬영 UI 및 OCR 개선 구현
  - 상태: 대기 중 (코드 리뷰)
  - 변경: +469줄 (6개 파일)
  - 이슈 연결: #54 #55 #56 (일부 #57)

---

## ✅ 체크리스트

### 이슈별 체크리스트

#### 이슈 #54 (완료)
- [x] ML Kit OCR 라이브러리 추가
- [x] 이미지 → InputImage 변환
- [x] OCR 텍스트 추출 로직 구현
- [x] EXIF 회전 처리 (추가 구현)

#### 이슈 #55 (완료)
- [x] OCR 결과 Element 단위 분리
- [x] 약 이름 파싱
- [x] 복약 정보 파싱 (용량, 횟수, 기간)
- [x] 텍스트 정규화

#### 이슈 #56 (완료)
- [x] ViewModel 상태 관리
- [x] OCR 결과 UI 표시
- [x] 촬영 → OCR → 결과 흐름 연결

#### 이슈 #57 (70% 완료)
- [x] 기본 예외 처리
- [x] 메모리 관리
- [x] 권한 처리
- [ ] 타임아웃 처리
- [ ] 저화질 이미지 심화 처리
- [ ] 사용자 안내 개선
- [ ] 로깅 추가

---

## 🚀 배포 준비 상태

| 항목 | 상태 | 비고 |
|---|---|---|
| 컴파일 | ✅ 성공 | `compileDebugKotlin` 통과 |
| 코드 스타일 | ✅ 통과 | `ktlintCheck` 통과 |
| 정적 분석 | ✅ 통과 | `detekt` 통과 |
| 단위 테스트 | ⏳ 미구현 | (향후 추가) |
| 통합 테스트 | ⏳ 미구현 | (향후 추가) |

---

**마지막 업데이트**: 2026-04-02
**작성자**: Claude Code
**상태**: 기본 기능 완료, 심화 작업 차기 일정
