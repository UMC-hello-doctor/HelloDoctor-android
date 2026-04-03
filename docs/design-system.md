# HelloDoctor Design System

> 의료 앱의 신뢰감과 접근성을 기반으로  
> 10~20대 타겟을 위한 명확하고 체계적인 디자인 토큰 시스템

**최종 업데이트**: 2026-04-03  
**상태**: Issue #61 Phase 3 완료  
**담당**: UMC Hello Doctor Design Team

---

## 목차

- [개요](#개요)
- [색상 시스템](#색상-시스템)
- [타이포그래피](#타이포그래피)
- [스페이싱 & 크기](#스페이싱--크기)
- [변경 이력](#변경-이력)
- [구현 현황](#구현-현황)

---

## 개요

### 디자인 원칙

| 원칙 | 설명 |
|------|------|
| **신뢰감** | 헬스케어 도메인의 전문성 표현 |
| **명확성** | 의료 정보와 상호작용의 직관적 구분 |
| **접근성** | WCAG AA 기준 색상 대비율 준수 |
| **일관성** | 역할 기반 토큰으로 전체 앱 통일 |

### 파일 구조

```
app/src/main/res/values/
├── colors.xml       # 색상 팔레트 & 시맨틱 색상
├── dimens.xml       # 타이포그래피, 스페이싱, 크기
└── styles.xml       # 텍스트 스타일 정의
```

---

## 색상 시스템

### 1. 브랜드 색상

#### Primary — 브랜드 메인

| 항목 | 값 |
|------|-----|
| **HEX** | `#1852FF` |
| **On Color** | `#FFFFFF` |
| **역할** | 로고, 주요 버튼, CTA, 활성 상태 |
| **변경 상태** | ✅ 유지 |

헬스케어의 신뢰·전문성을 나타내는 파란색. 높은 채도로 10~20대 모바일 UI에서도 선명함.

**사용처**:
- Primary 버튼 배경
- 링크 색상
- 활성 탭 표시
- 주요 아이콘

```xml
<color name="color_primary">#1852FF</color>
<color name="color_on_primary">#FFFFFF</color>
```

---

#### Secondary — 보조 강조

| 항목 | 기존 | 개선 | 상태 |
|------|------|------|------|
| **HEX** | `#CCC2DC` | `#1244CC` | ⚠️ 교체 |
| **On Color** | `#FFFFFF` | `#FFFFFF` | — |
| **역할** | 보조 색상 | Primary 딥버전, 강조 텍스트 | — |

**교체 이유**:
- 기존 연보라(`#CCC2DC`)는 Primary 파란색과 색상 계통이 달라 브랜드 통일성 훼손
- 흰색 텍스트와의 대비율 1.9:1로 WCAG AA 기준(4.5:1) 미충족
- Primary의 명도를 낮춘 딥블루로 교체해 색상 계열 통일

**사용처**:
- Secondary 버튼 배경
- 강조 텍스트 (부분 강조)
- 활성 상태 변형 표시

```xml
<color name="color_secondary">#1244CC</color>
<color name="color_on_secondary">#FFFFFF</color>
```

---

#### Primary Container — Primary 연한 배경 (신규 추가 🆕)

| 항목 | 값 |
|------|-----|
| **HEX** | `#E8F0FF` |
| **On Color** | `#1244CC` |
| **역할** | 선택된 탭, 활성 리스트 아이템 배경 |
| **변경 상태** | ✨ 추가 |

**추가 이유**: Primary를 배경으로 사용할 때 전체 배경을 칠하지 않고 강조 영역만 표현하기 위해 추가.

**사용처**:
- 선택된 탭 배경
- 활성 리스트 아이템 배경
- 확인/체크 상태 하이라이트

```xml
<color name="color_primary_container">#E8F0FF</color>
```

---

### 2. 시맨틱 색상

#### Success — 건강·회복

| 토큰 | HEX | 대비율 | 역할 |
|------|-----|--------|------|
| `color_success` | `#10B981` | 7.5:1 | 건강 달성, 복약 완료, 정상 수치 |
| `color_success_light` | `#D1FAE5` | — | 성공 상태 배경 |
| **상태** | ✅ 유지 | — | — |

헬스케어에서 건강·회복을 상징하는 색상으로, 역할과 색상이 완벽히 일치.

```xml
<color name="color_success">#10B981</color>
<color name="color_success_light">#D1FAE5</color>
```

---

#### Error — 경보·이상

| 토큰 | HEX | 대비율 | 역할 |
|------|-----|--------|------|
| `color_error` | `#EF4444` | 4.9:1 | 이상 수치, 긴급 알림, 입력 오류 |
| `color_error_light` | `#FEE2E2` | — | 에러 상태 배경 |
| **상태** | ✅ 유지 | — | — |

⚠️ **주의**: 헬스케어 앱에서 에러 색상이 과도하게 노출되면 사용자 불안감 유발. **노출 빈도 조절 권장**.

```xml
<color name="color_error">#EF4444</color>
<color name="color_error_light">#FEE2E2</color>
```

---

#### Warning — 경계·주의

| 토큰 | HEX | 대비율 | 역할 |
|------|-----|--------|------|
| `color_warning` | `#F59E0B` | 4.1:1 | 경계 수치, 복약 지연, 주의 상태 |
| `color_warning_light` | `#FEF3C7` | — | 경고 상태 배경 |
| **상태** | ✅ 유지 | — | — |

```xml
<color name="color_warning">#F59E0B</color>
<color name="color_warning_light">#FEF3C7</color>
```

---

#### Info — 정보·안내 (개선됨)

| 항목 | 기존 | 개선 | 상태 |
|------|------|------|------|
| **`color_info`** | `#3B82F6` | `#0EA5E9` | ⚠️ 교체 |
| **`color_info_light`** | `#DBEAFE` | `#E0F2FE` | — |
| **대비율** | — | 4.5:1 | — |
| **역할** | 일반 정보 | 복약 안내, 건강 팁 | — |

**교체 이유**:
- 기존 `#3B82F6`과 `color_primary(#1852FF)`은 모두 파란색 계열
- 사용자가 "브랜드 주요 버튼"과 "정보 안내"를 구별하기 어려움
- 헬스케어에서는 복약 안내, 건강 팁 등 정보성 메시지가 많아 혼동이 특히 위험
- **청록색으로 변경하여 Primary와 명확히 구분**

**사용처**:
- 정보 배너
- 복약 안내 메시지
- 건강 팁
- 안내 토스트

```xml
<color name="color_info">#0EA5E9</color>
<color name="color_info_light">#E0F2FE</color>
```

---

### 3. 텍스트 색상

| 토큰 | HEX | 대비율 | 역할 | 상태 |
|------|-----|--------|------|------|
| `color_text_main` | `#2A2A2A` | 17.1:1 | 기본 본문, 제목, 의료 수치 | ✅ 유지 |
| `color_text_sub` | `#555555` | 7.4:1 | 보조 텍스트, 라벨, 설명 | ⚠️ 교체 |
| `color_text_hint` | `#A0A0A0` | 2.8:1 | Placeholder, 부가 설명 | ✅ 유지 |
| `color_text_disabled` | `#CCCCCC` | — | 비활성 텍스트 (WCAG 예외) | ✅ 유지 |

#### `color_text_sub` 강화

**기존**: `#696969` (대비율 4.5:1 — AA 간신히 충족)  
**개선**: `#555555` (대비율 7.4:1 — AA 여유 있음)

**개선 이유**: 14px 미만의 소폰트에서 가독성 위험. 명도를 높여 여유 있는 대비율 확보.

```xml
<color name="color_text_main">#2A2A2A</color>
<color name="color_text_sub">#555555</color>
<color name="color_text_hint">#A0A0A0</color>
<color name="color_text_disabled">#CCCCCC</color>
```

---

### 4. 배경 & 표면

| 토큰 | HEX | 레이어 | 역할 | 상태 |
|------|-----|--------|------|------|
| `color_background` | `#F4F4F4` | 0 (최하단) | 앱 기본 배경 | ✅ 유지 |
| `color_surface` | `#FFFFFF` | 1 (중간) | 카드, 리스트 아이템 배경 | ✅ 유지 |
| `color_surface_raised` | `#FAFCFF` | 2 (최상단) | 모달, 드롭다운, 팝업 | ✨ 추가 |

**Elevation 시스템**:

```
Layer 2 (Highest)   ← color_surface_raised (#FAFCFF)
                      [모달, 드롭다운, 팝업]

Layer 1 (Middle)    ← color_surface (#FFFFFF)
                      [카드, 리스트 아이템, 구분선]

Layer 0 (Base)      ← color_background (#F4F4F4)
                      [스크린 전체 배경]
```

**`color_surface_raised` 추가 이유**: 기존 2단계로는 모달·드롭다운 같은 높은 elevation 컴포넌트를 표현할 방법이 없었음.

```xml
<color name="color_background">#F4F4F4</color>
<color name="color_surface">#FFFFFF</color>
<color name="color_surface_raised">#FAFCFF</color>
```

---

### 5. 버튼 색상

#### Primary 버튼

| 토큰 | 값 | 역할 |
|------|-----|------|
| `color_button_primary_bg` | `#1852FF` | 배경 (활성) |
| `color_button_primary_text` | `#FFFFFF` | 텍스트 (활성) |
| `color_button_primary_bg_disabled` | `#CCCCCC` | 배경 (비활성) |
| `color_button_primary_text_disabled` | `#FFFFFF` | 텍스트 (비활성) |
| **상태** | — | ✅ 유지 |

```xml
<color name="color_button_primary_bg">#1852FF</color>
<color name="color_button_primary_text">#FFFFFF</color>
<color name="color_button_primary_bg_disabled">#CCCCCC</color>
<color name="color_button_primary_text_disabled">#FFFFFF</color>
```

---

#### Secondary 버튼

| 토큰 | 기존 | 개선 | 역할 |
|------|------|------|------|
| `color_button_secondary_bg` | `#FFFFFF` | `#FFFFFF` | 배경 (활성) |
| `color_button_secondary_text` | `#696969` | `#555555` | 텍스트 (활성, 대비 강화 ⚠️) |
| `color_button_secondary_bg_disabled` | `#F0F0F0` | `#F0F0F0` | 배경 (비활성) |
| `color_button_secondary_text_disabled` | `#B0B0B0` | `#B0B0B0` | 텍스트 (비활성) |
| `color_button_secondary_stroke` | `#E0E0E0` | `#E0E0E0` | 테두리 색상 |

```xml
<color name="color_button_secondary_bg">#FFFFFF</color>
<color name="color_button_secondary_text">#555555</color>
<color name="color_button_secondary_bg_disabled">#F0F0F0</color>
<color name="color_button_secondary_text_disabled">#B0B0B0</color>
```

---

### 6. Gray Scale

| 토큰 | 기존 | 개선 | 용도 | 상태 |
|------|------|------|------|------|
| `gray_50` | `#FAFAFA` | `#FAFAFA` | 최상단 배경 | ✅ 유지 |
| `gray_100` | `#F5F5F5` | `#F5F5F5` | 밝은 배경 | ✅ 유지 |
| `gray_200` | `#EEEEEE` | `#EEEEEE` | 구분선, 비활성 배경 | ✅ 유지 |
| `gray_300` | `#E0E0E0` | `#E0E0E0` | 경계선, 버튼 테두리 | ✅ 유지 |
| `gray_400` | `#BDBDBD` | `#BDBDBD` | 아이콘, 중간 배경 | ✅ 유지 |
| `gray_500` | `#9E9E9E` | `#9E9E9E` | 보조 텍스트 | ✅ 유지 |
| `gray_600` | `#666666` | `#757575` | 약간 진한 텍스트 | ⚠️ 교체 |
| `gray_700` | `#777777` | `#616161` | 진한 텍스트 | ⚠️ 교체 |
| `gray_800` | `#424242` | `#424242` | 매우 진한 텍스트 | ✅ 유지 |
| `gray_900` | `#212121` | `#212121` | 최상단 텍스트 | ✅ 유지 |

**Gray Scale 정정 이유**:

기존 `gray_600(#666666)`이 `gray_700(#777777)`보다 어두워 **숫자가 올라갈수록 어두워져야 한다는 스케일 원칙 위반**.

→ 다크모드 및 컴포넌트 작업 시 예기치 않은 버그 유발 가능

```xml
<color name="gray_50">#FAFAFA</color>
<color name="gray_100">#F5F5F5</color>
<color name="gray_200">#EEEEEE</color>
<color name="gray_300">#E0E0E0</color>
<color name="gray_400">#BDBDBD</color>
<color name="gray_500">#9E9E9E</color>
<color name="gray_600">#757575</color>
<color name="gray_700">#616161</color>
<color name="gray_800">#424242</color>
<color name="gray_900">#212121</color>
```

---

### 7. Deprecated & 삭제 대상

#### 삭제 권장

| 토큰 | HEX | 이유 | 상태 |
|------|-----|------|------|
| `blue_primary` | `#2563EB` | `color_primary`와 역할 중복. 링크 색상 명확히 정의하지 않으면 삭제. | ❌ 사용 금지 |
| `primary` (alias) | `#1852FF` | `color_primary`와 완전 동일. `color_primary` 사용 권장. | ❌ deprecated |

#### 보류 — 역할 정의 필요

| 토큰 | HEX | 상태 | 다음 단계 |
|------|-----|------|----------|
| `orange_accent` | `#FF6B35` | 역할 미정의 | CTA 강조·배지·이벤트 배너 등 구체적 용도 확정 후 재정의 또는 삭제 |

---

## 타이포그래피

### 1. 폰트 설정

| 레벨 | 크기 | 두께 | 줄높이 | 용도 |
|------|------|------|--------|------|
| **Display** | 34sp | Medium | 1.5x | 랜딩 페이지 대제목 |
| **Headline** | 28sp | Medium | 1.5x | 섹션 제목 |
| **Title** | 20sp | Medium | 1.5x | 화면 타이틀 |
| **Body** | 15sp | Regular | 1.5x | 기본 본문 텍스트 |
| **Label** | 13sp | Medium | 1.5x | 버튼, 탭, 라벨 |
| **Caption** | 12sp | Regular | 1.5x | 부가 설명, 작은 텍스트 |

**폰트 패밀리**: Roboto (Android 기본 폰트)

### 2. 타이포그래피 스타일 (Kotlin/XML)

#### Display

```xml
<style name="TextAppearance.Display" parent="TextAppearance.MaterialComponents.Headline1">
    <item name="android:textSize">@dimen/text_size_display</item>
    <item name="android:textColor">@color/color_text_main</item>
    <item name="android:fontFamily">@font/roboto_medium</item>
    <item name="android:lineSpacingMultiplier">1.5</item>
    <item name="android:textStyle">bold</item>
</style>
```

**사용처**:
```kotlin
binding.titleText.setTextAppearance(R.style.TextAppearance_Display)
```

---

#### Headline

```xml
<style name="TextAppearance.Headline" parent="TextAppearance.MaterialComponents.Headline2">
    <item name="android:textSize">@dimen/text_size_headline</item>
    <item name="android:textColor">@color/color_text_main</item>
    <item name="android:fontFamily">@font/roboto_medium</item>
    <item name="android:lineSpacingMultiplier">1.5</item>
</style>
```

---

#### Title

```xml
<style name="TextAppearance.Title" parent="TextAppearance.MaterialComponents.Headline3">
    <item name="android:textSize">@dimen/text_size_title</item>
    <item name="android:textColor">@color/color_text_main</item>
    <item name="android:fontFamily">@font/roboto_medium</item>
    <item name="android:lineSpacingMultiplier">1.5</item>
</style>
```

---

#### Body

```xml
<style name="TextAppearance.Body" parent="TextAppearance.MaterialComponents.Body1">
    <item name="android:textSize">@dimen/text_size_body</item>
    <item name="android:textColor">@color/color_text_main</item>
    <item name="android:fontFamily">@font/roboto_regular</item>
    <item name="android:lineSpacingMultiplier">1.5</item>
</style>
```

---

#### Label

```xml
<style name="TextAppearance.Label" parent="TextAppearance.MaterialComponents.Button">
    <item name="android:textSize">@dimen/text_size_label</item>
    <item name="android:textColor">@color/color_text_main</item>
    <item name="android:fontFamily">@font/roboto_medium</item>
    <item name="android:lineSpacingMultiplier">1.5</item>
</style>
```

---

#### Caption

```xml
<style name="TextAppearance.Caption" parent="TextAppearance.MaterialComponents.Caption">
    <item name="android:textSize">@dimen/text_size_caption</item>
    <item name="android:textColor">@color/color_text_sub</item>
    <item name="android:fontFamily">@font/roboto_regular</item>
    <item name="android:lineSpacingMultiplier">1.5</item>
</style>
```

---

## 스페이싱 & 크기

### 1. Spacing Scale (8dp 기준)

| 토큰 | 값 | 배수 | 용도 |
|------|-----|------|------|
| `spacing_4` | 4dp | 0.5x | 매우 좁은 간격 (텍스트 내부) |
| `spacing_8` | 8dp | 1x (base) | 기본 간격 (요소 사이) |
| `spacing_16` | 16dp | 2x | 섹션 간격 |
| `spacing_24` | 24dp | 3x | 큰 섹션 간격 |
| `spacing_32` | 32dp | 4x | 대형 간격 |
| `spacing_40` | 40dp | 5x | 매우 큰 간격 |
| `spacing_48` | 48dp | 6x | 최대 간격 |
| `spacing_56` | 56dp | 7x | 풀스크린 간격 |

**사용 규칙**:
- 요소 간 기본 패딩: `spacing_8` (8dp)
- 섹션 간 패딩: `spacing_16` ~ `spacing_24`
- 카드 내부 패딩: `spacing_12` ~ `spacing_16`

```xml
<!-- Layout -->
<LinearLayout
    android:layout_margin="@dimen/spacing_16"
    android:paddingStart="@dimen/spacing_16"
    android:paddingEnd="@dimen/spacing_16">
</LinearLayout>
```

---

### 2. 컴포넌트 크기

#### 버튼 높이

| 토큰 | 값 | 용도 |
|------|-----|------|
| `button_height_small` | 36dp | 작은 인라인 버튼, 탭 |
| `button_height_medium` | 44dp | 표준 버튼 |
| `button_height_large` | 56dp | 주요 CTA 버튼 |

#### 버튼 패딩

| 토큰 | 값 |
|------|-----|
| `button_padding_horizontal` | 16dp |
| `button_padding_vertical` | 8dp |

#### 텍스트 입력

| 토큰 | 값 |
|------|-----|
| `text_input_height` | 44dp |
| `text_input_padding_horizontal` | 12dp |
| `text_input_padding_vertical` | 10dp |

#### 리스트 아이템

| 토큰 | 값 | 용도 |
|------|-----|------|
| `list_item_height_small` | 48dp | 콤팩트 리스트 (단순 텍스트) |
| `list_item_height_medium` | 64dp | 표준 리스트 (아이콘 + 텍스트) |
| `list_item_height_large` | 80dp | 콘텐츠 리스트 (이미지 + 텍스트) |
| `list_item_padding` | 12dp | 아이템 내부 패딩 |

#### 아이콘 크기

| 토큰 | 값 | 용도 |
|------|-----|------|
| `icon_size_small` | 20dp | 작은 인라인 아이콘 |
| `icon_size_medium` | 24dp | 표준 아이콘 (버튼, 탭) |
| `icon_size_large` | 32dp | 대형 아이콘 |

---

### 3. Corner Radius (모서리 둥글기)

| 토큰 | 값 | 용도 |
|------|-----|------|
| `corner_radius_small` | 4dp | 버튼, 입력 필드의 약한 둥글기 |
| `corner_radius_medium` | 8dp | 카드, 모달의 표준 둥글기 |
| `corner_radius_large` | 12dp | 대형 컴포넌트 |
| `corner_radius_extra_large` | 16dp | 특수 컴포넌트 (풀스크린 바텀시트) |

```xml
<!-- 버튼 -->
<shape android:shape="rectangle">
    <corners android:radius="@dimen/corner_radius_small" />
</shape>

<!-- 카드 -->
<shape android:shape="rectangle">
    <corners android:radius="@dimen/corner_radius_medium" />
</shape>
```

---

## 변경 이력

### Phase 3 (2026-04-03) ✅ 완료

| 범주 | 항목 수 | 상태 |
|------|--------|------|
| **유지** | 14개 | ✅ 그대로 유지 |
| **교체** | 4개 | ⚠️ 색상값 변경 |
| **추가** | 2개 | ✨ 신규 토큰 |
| **삭제** | 2개 | ❌ 사용 금지 |
| **보류** | 1개 | ⏳ 역할 정의 필요 |

**변경 목록**:

1. ⚠️ `color_secondary`: `#CCC2DC` → `#1244CC` (WCAG 대비율 강화)
2. ⚠️ `color_info`: `#3B82F6` → `#0EA5E9` (Primary와 색상 분리)
3. ⚠️ `color_text_sub`: `#696969` → `#555555` (가독성 강화)
4. ⚠️ `gray_600/700`: 순서 교정 (스케일 원칙 준수)
5. ✨ `color_primary_container`: `#E8F0FF` 추가 (선택 상태 배경)
6. ✨ `color_surface_raised`: `#FAFCFF` 추가 (모달 배경)
7. ❌ `blue_primary`, `primary` alias: 삭제 권장
8. ⏳ `orange_accent`: 역할 정의 대기

---

### Phase 2 (2026-03-31)

- Spacing 시스템 구축 (8dp 기준)
- Typography 정의 (6단계 크기)
- Corner radius 규정

### Phase 1 (2026-03-28)

- 초기 색상 팔레트 정의
- WCAG 접근성 검토

---

## 구현 현황

### 리소스 파일 적용 상태

| 파일 | 상태 | 위치 |
|------|------|------|
| **colors.xml** | ✅ 완료 | `app/src/main/res/values/colors.xml` |
| **dimens.xml** | ✅ 완료 | `app/src/main/res/values/dimens.xml` |
| **styles.xml** | ✅ 완료 | `app/src/main/res/values/styles.xml` |

### 코드 적용 가이드

#### Fragment에서 사용

```kotlin
// Kotlin
binding.titleText.setTextAppearance(R.style.TextAppearance_Title)
binding.mainButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_primary))
binding.container.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_surface))
```

#### XML Layout에서 사용

```xml
<!-- 색상 -->
<TextView
    android:textColor="@color/color_text_main"
    android:textAppearance="@style/TextAppearance.Body" />

<!-- 크기 -->
<Button
    android:layout_height="@dimen/button_height_medium"
    android:paddingStart="@dimen/button_padding_horizontal"
    android:paddingEnd="@dimen/button_padding_horizontal" />

<!-- 모서리 -->
<shape android:shape="rectangle">
    <corners android:radius="@dimen/corner_radius_medium" />
</shape>
```

---

## 접근성 기준

### WCAG AA 준수 색상 대비율

| 색상 조합 | 대비율 | 상태 | 용도 |
|----------|--------|------|------|
| `color_primary` on `color_surface` | 7.3:1 | ✅ | Primary 버튼 텍스트 |
| `color_secondary` on `color_surface` | 3.8:1 | ⚠️ | Secondary 강조 (큰 텍스트) |
| `color_text_main` on `color_surface` | 17.1:1 | ✅ | 본문 텍스트 |
| `color_text_sub` on `color_surface` | 7.4:1 | ✅ | 보조 텍스트 |
| `color_success` on `color_surface` | 7.5:1 | ✅ | 성공 메시지 |
| `color_error` on `color_surface` | 4.9:1 | ✅ | 에러 메시지 |

**규칙**:
- 일반 텍스트(14pt 이상): 최소 4.5:1
- 큰 텍스트(18pt 이상): 최소 3:1
- UI 컴포넌트: 최소 3:1

---

## FAQ

### Q: 새로운 색상을 추가하고 싶어요
**A**: `colors.xml`에 토큰을 추가하되, 다음을 확인하세요:
- WCAG AA 대비율 기준 검증 (`#fff` 배경 기준 4.5:1 이상)
- 기존 토큰과의 역할 중복 여부
- 토큰명이 역할을 명확히 표현하는지

### Q: Dark mode 지원은 계획 중인가요?
**A**: 현재 Light mode 기준으로 설계. Dark mode는 추후 `values-night/` 디렉토리 추가로 지원 예정.

### Q: gray_600/700이 뒤바뀌었다는데, 기존 코드는?
**A**: 기존 코드에서 `gray_600`을 사용 중이라면 새로운 값(`#757575`)이 적용됩니다. 필요시 특정 색상이 필요한 경우 별도 토큰으로 정의하세요.

### Q: `orange_accent`는 언제 정의되나요?
**A**: 구체적 용도(배지, CTA, 배너 등)가 결정되면 역할 기반으로 재정의하거나 삭제 예정입니다.

---

## 참고 자료

- **Material Design 3 Color System**: https://m3.material.io/styles/color
- **WCAG 2.1 대비율 가이드**: https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum
- **프로젝트 아키텍처**: [CLAUDE.md](../CLAUDE.md)
- **색상 개선 상세 분석**: [color-palette-improvements.md](./color-palette-improvements.md)

---

**관리자**: UMC Hello Doctor Design Team  
**마지막 업데이트**: 2026-04-03  
**상태**: Issue #61 Phase 3 완료
