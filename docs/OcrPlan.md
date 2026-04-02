# CameraX 기반 처방전 촬영 UI & OCR 개선 계획

## Context

현재 `PrescriptionScanFragment`는 시스템 카메라 앱에 위임하는 `ActivityResultContracts.TakePicture()` 방식을 사용하고 있어, 다음과 같은 한계가 있다:

1. **앱 내 카메라 프리뷰 없음** — 사용자가 사진 구도를 미리 볼 수 없음
2. **처방전 가이드 UI 불가** — 촬영 시 처방전을 정확히 정렬하는 가이드 프레임을 표시할 수 없음
3. **OCR 인식 정확도 저하** — 회전 또는 품질 낮은 이미지에서 약명 인식 오류 증가
4. **권한 거부 처리 미흡** — `onPermissionDenied = {}` 람다가 비어있어 사용자 피드백이 없음

**목표**: CameraX로 전환하여 앱 내 실시간 카메라 프리뷰 + 가이드 오버레이를 제공하고, EXIF 회전 처리 및 이미지 전처리로 OCR 한국어 인식 정확도를 향상시킨다.

---

## 변경 파일 요약

| 파일 | 변경 내용 | 영향도 |
|---|---|---|
| `app/build.gradle.kts` | CameraX 의존성 4줄 + ExifInterface 1줄 추가 | 📦 의존성 |
| `core/camera/CameraController.kt` | TakePicture 방식 → CameraX Preview + ImageCapture 전면 재작성 | 🎬 카메라 |
| `feature/drug/presentation/PrescriptionScanFragment.kt` | Compose UI: 버튼 전용 → 프리뷰 + 가이드 오버레이 재작성 | 🎨 UI |
| `feature/drug/data/repository/OcrRepositoryImpl.kt` | EXIF 회전 처리 + 이미지 전처리(그레이스케일/대비) 추가 | 🔍 OCR |
| `feature/drug/data/ocr/PrescriptionTextParser.kt` | 정규식 보강(번호 목록 패턴) + rawText 전처리 추가 | 📝 파싱 |

**변경 불필요**: `PrescriptionScanViewModel`, `ScanPrescriptionUseCase`, `OcrResultFragment`, `AndroidManifest.xml`, `file_paths.xml`
- 이유: `onImageCaptured(Uri)` 콜백 인터페이스가 유지되므로 하위 레이어 전체 영향 없음

---

## Step 1 — CameraX 의존성 추가

**파일**: `app/build.gradle.kts`

`dependencies` 블록에 다음 추가:

```gradle
// CameraX (처방전 실시간 프리뷰용)
implementation("androidx.camera:camera-core:1.4.2")
implementation("androidx.camera:camera-camera2:1.4.2")
implementation("androidx.camera:camera-lifecycle:1.4.2")
implementation("androidx.camera:camera-view:1.4.2")

// EXIF 회전 처리용
implementation("androidx.exifinterface:exifinterface:1.3.7")
```

---

## Step 2 — CameraController.kt 재작성

**경로**: `app/src/main/java/com/umc/hellodoctor/core/camera/CameraController.kt`

### 설계 변경: TakePicture → CameraX

| 항목 | 기존 | 변경 후 |
|---|---|---|
| 카메라 호출 방식 | `ActivityResultContracts.TakePicture()` (시스템 카메라 앱) | CameraX `Preview + ImageCapture` (앱 내 프리뷰) |
| 파일 저장 | `FileProvider` → 외부 저장소 | `context.cacheDir` (FileProvider 불필요) |
| 권한 요청 | `requestPermissionLauncher` | 동일 유지 |

### 생성자 시그니처

```kotlin
class CameraController(
    private val fragment: Fragment,
    private val onImageCaptured: (Uri) -> Unit,
    private val onPermissionDenied: () -> Unit = {},
    private val onPermissionGranted: () -> Unit = {},   // 신규
)
```

### 제거 항목

- `takePictureLauncher` — `ActivityResultContracts.TakePicture()`
- `photoUri: Uri?` — CameraX에서 파일 직접 관리
- `openCamera()` — 시스템 카메라 호출 로직

### 추가 항목

```kotlin
// 프로퍼티
private var imageCapture: ImageCapture? = null
private var cameraProvider: ProcessCameraProvider? = null

// 메서드
fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView)
  → ProcessCameraProvider 비동기 획득 (addListener 패턴)
  → Preview Use Case + ImageCapture Use Case 생성
  → cameraProvider.bindToLifecycle(lifecycleOwner, BACK_CAMERA, preview, imageCapture)

fun takePhoto()
  → imageCapture.takePicture(outputOptions, executor, OnImageSavedCallback)
  → 파일 저장 후 Uri.fromFile() → onImageCaptured(uri) 콜백

fun unbindCamera()
  → cameraProvider?.unbindAll()
```

### 권한 처리

- `checkPermissionAndOpenCamera()` 호출 시 권한 없으면 `requestPermissionLauncher.launch()`
- 권한 있거나 요청 후 승인 시 `onPermissionGranted()` 호출
- Fragment에서 `onPermissionGranted` 람다로 카메라 바인딩 처리

### ImageCapture 품질

```kotlin
val imageCapture = ImageCapture.Builder()
    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)  // 품질 우선
    .build()
```

---

## Step 3 — PrescriptionScanFragment.kt 재작성

**경로**: `app/src/main/java/com/umc/hellodoctor/feature/drug/presentation/PrescriptionScanFragment.kt`

### Fragment 클래스 변경

```kotlin
@AndroidEntryPoint
class PrescriptionScanFragment : Fragment() {
    private val viewModel: PrescriptionScanViewModel by viewModels()
    private lateinit var cameraController: CameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraController = CameraController(
            fragment = this,
            onImageCaptured = { uri -> viewModel.onImageCaptured(uri) },
            onPermissionDenied = {
                // 권한 거부 시 사용자 피드백 (기존: 빈 람다)
                Toast.makeText(
                    requireContext(),
                    "카메라 권한이 필요합니다.",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
            },
            onPermissionGranted = { /* bindCamera는 AndroidView factory에서 처리됨 */ },
        )
    }

    override fun onCreateView(...): View = ComposeView(requireContext()).apply {
        setContent {
            val uiState by viewModel.uiState.observeAsState(PrescriptionScanUiState.Idle)
            prescriptionScanScreen(
                uiState = uiState,
                onCheckPermission = { cameraController.checkPermissionAndOpenCamera() },
                onViewResult = { /* 기존 코드 */ },
                onBack = { findNavController().popBackStack() },
            )
        }
    }

    // 신규 추가: CameraX unbind
    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.unbindCamera()
    }
}
```

### Compose UI 재설계

```
Box(fillMaxSize) {
    when (uiState) {
        Idle →
            Column {
                // 1. 카메라 프리뷰 레이어
                AndroidView(
                    factory = { context ->
                        PreviewView(context).also { previewView ->
                            cameraController.bindCamera(viewLifecycleOwner, previewView)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. 가이드 오버레이 레이어
                PrescriptionGuideOverlay(modifier = Modifier.fillMaxSize())

                // 3. 촬영 버튼 (하단 중앙)
                CaptureButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    onClick = { cameraController.takePhoto() }
                )

                // 4. 뒤로가기 버튼 (상단 좌측)
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    onClick = onBack
                ) {
                    Icon(Icons.Default.ArrowBack, "뒤로가기")
                }
            }

        Loading →
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("처방전을 분석 중입니다…")
            }

        is Success →
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "약 ${uiState.result.recognizedMedicineNames.size}개를 인식했습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { onViewResult(uiState.result) }) {
                    Text("결과 보기")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.resetToIdle() }) {
                    Text("다시 촬영")
                }
            }

        is Error →
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "오류: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { viewModel.resetToIdle() }) {
                    Text("다시 시도")
                }
            }
    }
}
```

### 신규 Composable (private)

**PrescriptionGuideOverlay** — 반투명 배경 + 직사각형 가이드 프레임

```kotlin
@Composable
private fun PrescriptionGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen  // API 26+
    }) {
        // 1. 전체 반투명 검은색 배경
        drawRect(color = Color.Black.copy(alpha = 0.5f))

        // 2. 가이드 직사각형 좌표 계산
        // 화면 너비의 90%, 가로세로 비율 1:1.4 (A4 비율)
        val rectWidth = size.width * 0.9f
        val rectHeight = rectWidth * 1.4f
        val left = (size.width - rectWidth) / 2f
        val top = (size.height - rectHeight) / 2f

        // 3. 가이드 영역을 투명하게 뚫기 (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                blendMode = BlendMode.Clear
            )
        }

        // 4. 가이드 테두리
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
```

**CaptureButton** — 72dp 원형 흰색 버튼

```kotlin
@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Color.LightGray, CircleShape)
            .clickable(onClick = onClick),
    )
}
```

### ViewModel 추가

`PrescriptionScanViewModel.kt`에 다음 메서드 추가:

```kotlin
fun resetToIdle() {
    _uiState.value = PrescriptionScanUiState.Idle
}
```

---

## Step 4 — OCR 한국어 인식 정확도 향상

### 4-1. 이미지 회전 처리

**파일**: `feature/drug/data/repository/OcrRepositoryImpl.kt`

**문제**: `InputImage.fromFilePath(context, uri)`는 `file://` URI의 EXIF 회전 정보를 신뢰할 수 없음

**해결**: EXIF 정보를 직접 읽어 `InputImage.fromBitmap(bitmap, rotationDegrees)` 사용

```kotlin
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

@Suppress("TooGenericExceptionCaught")
override suspend fun recognizeText(imageUri: Uri): Result<OcrResult> =
    suspendCancellableCoroutine { cont ->
        try {
            // 이미지 파일 경로 추출
            val filePath = imageUri.path ?: throw IOException("Invalid URI: $imageUri")

            // EXIF 회전 정보 읽기
            val exif = ExifInterface(filePath)
            val rotationDegrees = when (
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            // 비트맵 로드 후 전처리
            val originalBitmap = BitmapFactory.decodeFile(filePath)
                ?: throw IOException("Failed to decode image at $filePath")

            val preprocessedBitmap = preprocessBitmap(originalBitmap)
            val image = InputImage.fromBitmap(preprocessedBitmap, rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    preprocessedBitmap.recycle()  // 메모리 해제
                    originalBitmap.recycle()
                    cont.resume(Result.success(parser.parse(visionText.text)))
                }
                .addOnFailureListener { exception ->
                    preprocessedBitmap.recycle()
                    originalBitmap.recycle()
                    cont.resume(Result.failure(exception))
                }
        } catch (e: IllegalArgumentException) {
            cont.resume(Result.failure(e))
        } catch (e: IOException) {
            cont.resume(Result.failure(e))
        } catch (e: RuntimeException) {
            cont.resume(Result.failure(e))
        }
    }
```

### 4-2. 이미지 전처리 — 그레이스케일 + 대비 강화

ML Kit 한국어 OCR은 고대비 흑백 이미지에서 인식률 향상. `OcrRepositoryImpl`에 다음 메서드 추가:

```kotlin
private fun preprocessBitmap(src: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint()

    // 그레이스케일 변환
    val grayScale = android.graphics.ColorMatrix()
    grayScale.setSaturation(0f)

    // 대비 강화: scale=1.5, translate=-50
    val contrast = android.graphics.ColorMatrix(
        floatArrayOf(
            1.5f, 0f, 0f, 0f, -50f,
            0f, 1.5f, 0f, 0f, -50f,
            0f, 0f, 1.5f, 0f, -50f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    grayScale.postConcat(contrast)
    paint.colorFilter = android.graphics.ColorMatrixColorFilter(grayScale)
    canvas.drawBitmap(src, 0f, 0f, paint)

    return output
}
```

### 4-3. PrescriptionTextParser 정규식 보강

**파일**: `feature/drug/data/ocr/PrescriptionTextParser.kt`

현재 정규식은 접미사 기반(`정|캡슐|...`)이라 번호 목록 형식의 약명을 누락. 다음 패턴 추가:

```kotlin
companion object {
    // 기존
    private val MEDICINE_SUFFIX_REGEX = Regex(
        """[가-힣a-zA-Z0-9\s]+(?:정|캡슐|시럽|액|연고|크림|주사|패치|산|환|침|좌제|흡입제)(?:\([^)]*\))?"""
    )

    // 신규: 번호 목록 형식 (예: "1. 아스피린정")
    private val NUMBERED_MEDICINE_REGEX = Regex(
        """^\d+[..)]\s*([가-힣a-zA-Z0-9\s]+(?:정|캡슐|시럽|액|연고|크림|주사|패치|산|환|좌제|흡입제))""",
        RegexOption.MULTILINE
    )

    // 기존 정규식들...
    private val FREQUENCY_REGEX = Regex("""1일\s*(\d+)\s*회""")
    // ...
}

fun parse(rawText: String): OcrResult {
    // 1. rawText 전처리
    val normalized = rawText
        .replace(Regex("""[^\S\n]+"""), " ")  // 연속 공백 → 단일 공백 (줄바꿈 유지)
        .replace(Regex("""(\d)\s+([가-힣])"""), "$1$2")  // "1 약" → "1약"

    // 2. 기존 방식으로 추출
    val medicinesSuffix = MEDICINE_SUFFIX_REGEX.findAll(normalized)
        .map { it.value.trim() }
        .filter { it.length >= MIN_MEDICINE_NAME_LENGTH }
        .toSet()

    // 3. 번호 목록 형식으로도 추출
    val medicinesNumbered = NUMBERED_MEDICINE_REGEX.findAll(normalized)
        .mapNotNull { it.groupValues.getOrNull(1)?.trim() }
        .filter { it.length >= MIN_MEDICINE_NAME_LENGTH }
        .toSet()

    // 4. 두 패턴 모두 포함
    val medicines = (medicinesSuffix + medicinesNumbered)
        .distinct()
        .toList()

    return OcrResult(
        rawText = normalized,
        recognizedMedicineNames = medicines,
        frequencyHint = FREQUENCY_REGEX.find(normalized)?.groupValues?.get(1)?.let { "${it}회/일" },
        dosageHint = DOSAGE_REGEX.find(normalized)?.value,
        durationDays = DURATION_REGEX.find(normalized)?.groupValues?.get(1)?.toIntOrNull(),
    )
}
```

---

## Step 5 — 주의사항 및 주의깊을 점

### CameraX 관련

1. **`ProcessCameraProvider.getInstance().get()` 블로킹 금지**
   - 메인 스레드에서 `.get()`을 직접 호출하면 ANR 발생
   - `addListener()` 비동기 패턴 사용 필수

2. **`BlendMode.Clear` API 호환성**
   - API 26+ 이상에서만 `BlendMode.Clear` 지원
   - `minSdk=24` 환경이므로 API 24-25 기기에서는 `Stroke(테두리)` 만 표시하는 fallback 필요
   ```kotlin
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
       drawRect(..., blendMode = BlendMode.Clear)
   }
   ```

3. **AndroidView 재생성 시 bindCamera 재호출**
   - `uiState`가 `Idle` → (Loading/Success/Error) → `Idle`로 복귀 시 `AndroidView`가 재생성될 수 있음
   - `bindCamera()` 내부에서 `cameraProvider?.unbindAll()` 후 재바인딩

4. **`CompositingStrategy.Offscreen`**
   - `BlendMode.Clear`와 함께 사용하려면 필수
   - API 26+ 전용

### OCR/이미지 처리 관련

5. **Bitmap 메모리 관리**
   - `preprocessBitmap()` 후 원본 및 전처리 비트맵에 `.recycle()` 호출
   - 메모리 누수 방지

6. **EXIF 회전 정보**
   - 모든 카메라가 올바른 EXIF 정보를 기록하지는 않음
   - 기기/카메라 앱에 따라 회전 정보가 없을 수 있음 → fallback으로 0도 처리

---

## 검증 방법

### 컴파일 및 코드 품질

```bash
# 컴파일 확인
./gradlew compileDebugKotlin

# 코드 스타일 확인
./gradlew ktlintCheck

# 정적 분석
./gradlew detekt
```

### 수동 테스트 (실기기 또는 에뮬레이터)

1. **카메라 권한**
   - DrugAlarmFragment → 처방전 스캔 진입
   - 카메라 권한 있으면 → 카메라 프리뷰 표시 ✓

2. **카메라 프리뷰 및 UI**
   - 가이드 직사각형 오버레이 표시 ✓
   - 상단 좌측 뒤로가기 버튼 ✓
   - 하단 중앙 원형 촬영 버튼 ✓

3. **촬영 및 OCR**
   - 촬영 버튼 클릭 → Loading 상태 표시
   - OCR 분석 완료 → Success 상태 + "약 N개 인식" ✓
   - "결과 보기" 클릭 → OcrResultFragment 이동 ✓

4. **권한 거부 처리**
   - 카메라 권한 없을 때 → Toast 표시 + popBackStack ✓

5. **재촬영 흐름**
   - Success 상태에서 "다시 촬영" 클릭 → 카메라 프리뷰로 복귀 ✓

6. **회전 처리**
   - 기기를 세로/가로로 회전한 상태에서 촬영
   - 약명이 정상 방향으로 인식 ✓

7. **OCR 정확도 개선**
   - 실제 처방전 이미지로 테스트
   - 기존 대비 약명 인식 개수 및 정확도 비교

---

## 타임라인 추정

| 항목 | 예상 시간 |
|---|---|
| CameraX 의존성 추가 + CameraController 재작성 | 2-3시간 |
| PrescriptionScanFragment UI 재작성 | 2-3시간 |
| OcrRepositoryImpl + PrescriptionTextParser 개선 | 1-2시간 |
| 수동 테스트 및 버그 수정 | 1-2시간 |
| **총계** | **6-10시간** |

---

## 참고 자료

- [CameraX 공식 문서](https://developer.android.com/jetpack/androidx/releases/camera)
- [ML Kit Text Recognition (Korean)](https://developers.google.com/ml-kit/vision/text-recognition/korean)
- [Android ExifInterface](https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface)
- [Jetpack Compose Canvas](https://developer.android.com/reference/kotlin/androidx/compose/foundation/Canvas)
