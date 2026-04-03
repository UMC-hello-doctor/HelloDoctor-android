# Design System Compose 도입 계획 — 폴더 구조 정리 및 마이그레이션

**Issue**: #62 (ComposeView 도입) + #61 (Design System) 통합
**Depends on**: #59, #61  
**Status**: 계획 수립  
**Last Updated**: 2026-04-03

---

## 📋 목차

1. [현재 상태 분석](#현재-상태-분석)
2. [목표](#목표)
3. [Compose 기반 Design System 구조](#compose-기반-design-system-구조)
4. [파일 구성 세부사항](#파일-구성-세부사항)
5. [마이그레이션 로드맵](#마이그레이션-로드맵)
6. [구현 체크리스트](#구현-체크리스트)

---

## 현재 상태 분석

### XML 기반 Design System (현재)

```
📁 app/src/main/res/
├── values/
│   ├── colors.xml          # 52개 색상 정의 (혼재 상태)
│   ├── dimens.xml          # 4개 dimen (text_size 3개 + corner 1개)
│   └── themes.xml          # Material3 기반, 하드코딩 많음 (15.35dp, 21dp 등)
│
└── values-night/
    └── themes.xml          # Dark theme 분리

📁 app/src/main/java/.../core/design_system/
├── component/              # ❌ 비어있음
├── theme/                  # ❌ 비어있음
└── token/                  # ❌ 비어있음
```

### 주요 문제점

| 문제 | 영향 | 우선순위 |
|------|------|---------|
| **접근성 미충족** (대비율 기준 미만) | WCAG 위반, 법적 리스크 | 🔴 HIGH |
| **색상 계통 혼동** (primary vs info 둘 다 파란색) | UX 혼동 | 🔴 HIGH |
| **Gray Scale 역전** | 다크모드/컴포넌트 버그 유발 | 🟡 MEDIUM |
| **Spacing System 부재** | 하드코딩 (15.35dp, 21dp, 14dp, 12dp 등) | 🟡 MEDIUM |
| **Typography 분산** | font-family, line-height 미정의 | 🟡 MEDIUM |
| **XML 기반 제약** | Compose 도입 시 중복 관리 필요 | 🟡 MEDIUM |

---

## 목표

### Phase 1: 색상 개선 (즉시, #61)
- WCAG 접근성 기준 충족
- 색상 계통 명확화
- Gray Scale 정정

### Phase 2: XML → Kotlin 마이그레이션 (현재, #62 일부)
- Design System 패키지 구조화
- Token 정의 (Kotlin Data Classes)
- Compose Material3 Material3 Theme 준비

### Phase 3: ComposeView 통합 (현재, #62 메인)
- Fragment에 ComposeView 추가
- Theme 연동
- 기존 뷰와 Compose 상호작용

---

## Compose 기반 Design System 구조

### 목표 구조

```
📁 app/src/main/java/com/umc/hellodoctor/core/design_system/

├── 📁 token/                    # Design Tokens (최하층, 데이터)
│   ├── Color.kt                 # 색상 팔레트 정의 (sealed class + object)
│   ├── Dimension.kt             # 간격/크기 토큰 (8dp scale)
│   ├── Typography.kt            # 타이포그래피 정의
│   └── Elevation.kt             # Shadow/Elevation 토큰
│
├── 📁 theme/                    # Material3 Theme (토큰 조합)
│   ├── Theme.kt                 # Material3 ColorScheme + 커스텀
│   ├── Color.kt                 # Material3 Light/Dark ColorScheme 정의
│   ├── Typography.kt            # Material3 Typography (Display/Title/Body/...)
│   ├── Shape.kt                 # Material3 Shapes (cornerRadius)
│   └── HelloDoctorTheme.kt       # 통합 Theme Composable
│
├── 📁 component/                # Compose Components (UI 레이어)
│   ├── button/
│   │   ├── PrimaryButton.kt
│   │   └── SecondaryButton.kt
│   ├── card/
│   │   ├── BaseCard.kt
│   │   └── HealthCard.kt
│   ├── input/
│   │   ├── TextInputField.kt
│   │   └── OutlinedTextField.kt
│   ├── dialog/
│   │   └── ConfirmDialog.kt
│   ├── text/
│   │   └── TextStyles.kt
│   └── README.md                 # Component 문서
│
└── 📁 ui/                       # UI 유틸리티 (선택)
    ├── Modifiers.kt             # 자주 쓰는 modifier 조합
    └── Preview.kt               # Preview helpers
```

---

## 파일 구성 세부사항

### 1. Token Layer — `token/Color.kt`

```kotlin
// app/src/main/java/com/umc/hellodoctor/core/design_system/token/Color.kt
package com.umc.hellodoctor.core.design_system.token

import androidx.compose.ui.graphics.Color

/**
 * HelloDoctor Color Palette
 * Based on colors.xml + Phase 1 improvements (Issue #61)
 */
object HelloDoctorColors {
    // ==================== PRIMARY ====================
    val Primary = Color(0xFF1852FF)              // 브랜드 메인
    val OnPrimary = Color(0xFFFFFFFF)            // Primary 위 텍스트
    val PrimaryContainer = Color(0xFFE8F0FF)    // 선택/활성 배경

    // ==================== SECONDARY ====================
    val Secondary = Color(0xFF1244CC)            // 개선: #CCC2DC → 대비율 개선
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE8F0FF)

    // ==================== TERTIARY ====================
    val Tertiary = Color(0xFF0EA5E9)             // 개선: #3B82F6 → 분리
    val OnTertiary = Color(0xFFFFFFFF)

    // ==================== ERROR ====================
    val Error = Color(0xFFDC2626)                // 에러/위험
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFEEAEA)

    // ==================== TEXT ====================
    val TextMain = Color(0xFF2A2A2A)             // 본문 텍스트
    val TextSub = Color(0xFF555555)              // 개선: #696969 → 가독성 강화

    // ==================== BACKGROUND ====================
    val Background = Color(0xFFF4F4F4)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceRaised = Color(0xFFFAFCFF)       // 새로 추가: 모달/드롭다운

    // ==================== GRAY SCALE (8-step) ====================
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)             // 추가
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)             // 개선: #666666 → 스케일 정정
    val Gray700 = Color(0xFF616161)             // 개선: #777777 → 스케일 정정
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)             // 추가

    // ==================== BUTTON STATES ====================
    val ButtonPrimaryBg = Color(0xFF1852FF)
    val ButtonPrimaryText = Color(0xFFFFFFFF)
    val ButtonPrimaryBgDisabled = Color(0xFFCCCCCC)

    val ButtonSecondaryBg = Color(0xFFFFFFFF)
    val ButtonSecondaryText = Color(0xFF555555)  // 개선: #696969 → 가독성
    val ButtonSecondaryBgDisabled = Color(0xFFF0F0F0)

    // ==================== SEMANTIC COLORS ====================
    val Success = Color(0xFF10B981)              // 성공/확인
    val Warning = Color(0xFFF59E0B)              // 경고
    val Info = Color(0xFF0EA5E9)                 // 정보
}
```

### 2. Token Layer — `token/Dimension.kt`

```kotlin
// app/src/main/java/com/umc/hellodoctor/core/design_system/token/Dimension.kt
package com.umc.hellodoctor.core.design_system.token

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HelloDoctor Dimension System (8dp scale)
 * Spacing: 4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 32dp, 40dp, 48dp
 */
object HelloDoctorDimensions {
    // ==================== SPACING ====================
    val Spacing0 = 0.dp          // 없음
    val Spacing2 = 2.dp          // 극소
    val Spacing4 = 4.dp          // 아주 작음
    val Spacing8 = 8.dp          // 작음
    val Spacing12 = 12.dp        // 기본
    val Spacing16 = 16.dp        // 중간
    val Spacing20 = 20.dp        // 큼
    val Spacing24 = 24.dp        // 더 큼
    val Spacing32 = 32.dp        // 매우 큼
    val Spacing40 = 40.dp        // 초대형
    val Spacing48 = 48.dp        // 최대

    // ==================== CORNER RADIUS ====================
    val CornerSmall = 8.dp       // 작은 모서리
    val CornerMedium = 12.dp     // 중간 모서리
    val CornerLarge = 15.dp      // 큰 모서리 (기존 chat_corner)
    val CornerXLarge = 20.dp     // 초대형 모서리 (카드)

    // ==================== TYPOGRAPHY ====================
    val TextSizeCaption = 12.sp  // 작은 보조 텍스트
    val TextSizeBody = 15.sp     // 기본 본문
    val TextSizeTitle = 20.sp    // 화면 타이틀
    val TextSizeHeadline = 24.sp // 헤드라인 (새로 추가)
    val TextSizeDisplay = 32.sp  // 디스플레이 (새로 추가)

    // ==================== BUTTON SIZES ====================
    val ButtonHeight = 50.dp     // 주요 버튼
    val ButtonHeightSmall = 40.dp // 작은 버튼
    val ChoiceButtonHeight = 48.dp // 선택 버튼

    // ==================== COMPONENT SIZES ====================
    val IconSizeSmall = 16.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp

    // ==================== ELEVATION ====================
    val ElevationNone = 0.dp
    val ElevationSmall = 2.dp
    val ElevationMedium = 4.dp
    val ElevationLarge = 8.dp
}
```

### 3. Theme Layer — `theme/Theme.kt` (핵심)

```kotlin
// app/src/main/java/com/umc/hellodoctor/core/design_system/theme/Theme.kt
package com.umc.hellodoctor.core.design_system.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.umc.hellodoctor.core.design_system.token.HelloDoctorColors
import com.umc.hellodoctor.core.design_system.token.HelloDoctorDimensions

// Light Color Scheme
val LightColorScheme = ColorScheme(
    primary = HelloDoctorColors.Primary,
    onPrimary = HelloDoctorColors.OnPrimary,
    primaryContainer = HelloDoctorColors.PrimaryContainer,
    onPrimaryContainer = HelloDoctorColors.TextMain,
    
    secondary = HelloDoctorColors.Secondary,
    onSecondary = HelloDoctorColors.OnSecondary,
    secondaryContainer = HelloDoctorColors.SecondaryContainer,
    onSecondaryContainer = HelloDoctorColors.TextMain,
    
    tertiary = HelloDoctorColors.Tertiary,
    onTertiary = HelloDoctorColors.OnTertiary,
    tertiaryContainer = HelloDoctorColors.Gray100,
    onTertiaryContainer = HelloDoctorColors.TextMain,
    
    error = HelloDoctorColors.Error,
    onError = HelloDoctorColors.OnError,
    errorContainer = HelloDoctorColors.ErrorContainer,
    onErrorContainer = HelloDoctorColors.Error,
    
    background = HelloDoctorColors.Background,
    onBackground = HelloDoctorColors.TextMain,
    
    surface = HelloDoctorColors.Surface,
    onSurface = HelloDoctorColors.TextMain,
    surfaceVariant = HelloDoctorColors.Gray100,
    onSurfaceVariant = HelloDoctorColors.TextSub,
    
    outline = HelloDoctorColors.Gray400,
    outlineVariant = HelloDoctorColors.Gray200,
    
    scrim = HelloDoctorColors.Gray900.copy(alpha = 0.32f)
)

// Dark Color Scheme (향후 확장)
val DarkColorScheme = ColorScheme(
    // TODO: Dark 팔레트 정의
)

// Material3 Shapes
val HelloDoctorShapes = Shapes(
    extraSmall = RoundedCornerShape(HelloDoctorDimensions.CornerSmall),
    small = RoundedCornerShape(HelloDoctorDimensions.CornerMedium),
    medium = RoundedCornerShape(HelloDoctorDimensions.CornerLarge),
    large = RoundedCornerShape(HelloDoctorDimensions.CornerXLarge),
    extraLarge = RoundedCornerShape(HelloDoctorDimensions.CornerXLarge)
)

// Material3 Typography
val HelloDoctorTypography = Typography(
    displayLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeDisplay,
        lineHeight = 40.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeTitle,
        lineHeight = 28.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeBody,
        lineHeight = 24.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal
    ),
    labelSmall = TextStyle(
        fontSize = HelloDoctorDimensions.TextSizeCaption,
        lineHeight = 16.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium
    )
    // ... 추가 스타일
)

/**
 * HelloDoctor Theme — Material3 기반 통합 테마
 * 
 * 사용:
 * ```
 * HelloDoctorTheme {
 *   // Compose 콘텐츠
 * }
 * ```
 */
@Composable
fun HelloDoctorTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = HelloDoctorShapes,
        typography = HelloDoctorTypography,
        content = content
    )
}
```

### 4. Component Layer — `component/button/PrimaryButton.kt`

```kotlin
// app/src/main/java/com/umc/hellodoctor/core/design_system/component/button/PrimaryButton.kt
package com.umc.hellodoctor.core.design_system.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.umc.hellodoctor.core.design_system.token.HelloDoctorColors
import com.umc.hellodoctor.core.design_system.token.HelloDoctorDimensions

/**
 * HelloDoctor Primary Button
 * 
 * 주요 액션 버튼 (확인, 다음, 전송 등)
 * 
 * @param text 버튼 텍스트
 * @param onClick 클릭 콜백
 * @param modifier 커스텀 modifier
 * @param enabled 활성 여부
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(HelloDoctorDimensions.ButtonHeight)
        .padding(
            horizontal = HelloDoctorDimensions.Spacing20,
            vertical = HelloDoctorDimensions.Spacing8
        ),
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) HelloDoctorColors.Primary 
                           else HelloDoctorColors.ButtonPrimaryBgDisabled,
            contentColor = HelloDoctorColors.OnPrimary,
            disabledContainerColor = HelloDoctorColors.ButtonPrimaryBgDisabled,
            disabledContentColor = HelloDoctorColors.ButtonPrimaryText
        ),
        shape = RoundedCornerShape(HelloDoctorDimensions.CornerLarge)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = HelloDoctorDimensions.TextSizeBody
        )
    }
}
```

---

## 마이그레이션 로드맵

### Phase 1: Token 정의 (1-2일)
```
✅ Color.kt 작성
✅ Dimension.kt 작성
- Elevation.kt 작성
- Migration: colors.xml → Color.kt 매핑 검증
```

### Phase 2: Theme 구축 (2-3일)
```
- Theme.kt 작성 (Material3 ColorScheme, Typography, Shapes)
- HelloDoctorTheme Composable 작성
- Light/Dark theme 정의
- Preview 확인
```

### Phase 3: Core Components (3-5일)
```
- Button (Primary, Secondary)
- Card (Base, Health)
- Input (TextField)
- Dialog
- Text styles
```

### Phase 4: ComposeView 통합 (현재 Issue #62)
```
- Fragment에 ComposeView 추가
- Theme 적용
- 기존 뷰와 상호작용
```

### Phase 5: 마이그레이션 (점진적)
```
- 기존 Fragment UI → ComposeView로 변경
- styles.xml → Material3 theme로 대체
- colors.xml 중복 제거 가능
```

---

## 구현 체크리스트

### Issue #62 진행 중 체크리스트

#### Token Layer
- [ ] `token/Color.kt` 작성 (Phase 1 색상 개선 반영)
- [ ] `token/Dimension.kt` 작성 (8dp scale)
- [ ] `token/Typography.kt` 작성 (선택)
- [ ] `token/Elevation.kt` 작성 (선택)

#### Theme Layer
- [ ] `theme/Theme.kt` 작성 (Material3 ColorScheme)
- [ ] `theme/HelloDoctorTheme.kt` 작성 (Composable)
- [ ] Dark theme 정의

#### Core Components
- [ ] `component/button/PrimaryButton.kt`
- [ ] `component/button/SecondaryButton.kt`
- [ ] `component/card/BaseCard.kt`
- [ ] `component/README.md` (컴포넌트 문서)

#### Fragment 통합
- [ ] Fragment에 ComposeView 추가
- [ ] HelloDoctorTheme 적용
- [ ] 기존 View와 Compose 간 데이터 바인딩
- [ ] 테스트 (크래시 없음 확인)

#### 문서화
- [ ] Design System 가이드 (이 문서)
- [ ] Component API 문서
- [ ] 마이그레이션 가이드

---

## XML → Kotlin 매핑 참고

### colors.xml → Color.kt

| XML 항목 | Kotlin 항목 | 비고 |
|---------|-----------|------|
| `color_primary` | `HelloDoctorColors.Primary` | 동일 |
| `color_secondary` | `HelloDoctorColors.Secondary` | Phase 1: 개선 예정 |
| `color_text_main` | `HelloDoctorColors.TextMain` | 동일 |
| `color_button_*` | `HelloDoctorColors.Button*` | 그룹화 |

### dimens.xml → Dimension.kt

| XML 항목 | Kotlin 항목 | 비고 |
|---------|-----------|------|
| `text_size_caption` | `TextSizeCaption` | 동일 |
| `text_size_body` | `TextSizeBody` | 동일 |
| `text_size_title` | `TextSizeTitle` | 동일 |
| `chat_corner` | `CornerLarge` | 이름 변경 |
| (하드코딩) | `Spacing*` | 새로 정의 |

---

## 주의사항

1. **Phase 1 색상 개선 의존**: 
   - Issue #61의 Phase 1 (색상) 개선 사항 반영 필수
   - `color_secondary`, `color_info`, `color_text_sub` 등 개선 필요

2. **Material3 호환성**:
   - Material3 ColorScheme 필드명 준수 (primary, secondary, tertiary, error 등)
   - RoundedCornerShape, TextStyle 임포트 확인

3. **Backward Compatibility**:
   - 기존 XML 리소스 병행 유지 (Fragment의 XML 뷰도 사용 중)
   - 점진적 마이그레이션 진행

4. **Preview 확인**:
   - Compose Preview로 각 컴포넌트 시각 확인
   - 라이트/다크 테마 모두 테스트

---

## 참고 자료

- [Material Design 3 Compose Documentation](https://developer.android.com/jetpack/compose/designsystems/material3)
- [HelloDoctor Design System — Issue #61](../../.claude/rules/architecture.md)
- [Issue #59 — XML → Compose 마이그레이션](../../issues/59)
- `docs/color-palette-improvements.md` (Phase 1 색상 개선)

---

**다음 단계**: Token/Theme 파일 작성 시작 → Fragment ComposeView 통합
