# 디자인 시스템 개선안

**이슈**: #61 [Design] 디자인 시스템 (design)
**작성일**: 2026-04-03
**상태**: 분석 및 개선안 제시

---

## 목차
1. [현재 상태 분석](#현재-상태-분석)
2. [주요 문제점](#주요-문제점)
3. [개선 방향](#개선-방향)
4. [구체적 개선안](#구체적-개선안)
5. [구현 로드맵](#구현-로드맵)

---

## 현재 상태 분석

### ✅ 잘 정의된 부분

#### 1. **colors.xml** (기본 완성)
- 기본 색상: black, white
- 브랜드 색상: color_primary (#1852FF), color_secondary (#CCC2DC)
- 텍스트 색상: color_text_main, color_text_sub
- 버튼 색상: primary, secondary (활성/비활성 상태)
- Gray 톤: 5단계 (50, 100, 200, 400, 500, 800)
- 추가 색상: blue_primary, orange_accent

**평가**: 기본 색상 시스템은 잘 구성됨

#### 2. **themes.xml** (부분 구성)
- Button 기본 스타일 정의
- Card 스타일 (HealthCard)
- TextInputLayout 스타일
- 선택 버튼 (ChoiceButton) 스타일

**평가**: 컴포넌트별 스타일은 있으나 연관성이 약함

### ❌ 부족하거나 분산된 부분

#### 1. **dimens.xml** (매우 부족)
현재 정의:
```xml
<dimen name="text_size_caption">12sp</dimen>
<dimen name="text_size_body">15sp</dimen>
<dimen name="text_size_title">20sp</dimen>
<dimen name="chat_coner">15dp</dimen>  <!-- typo: "coner" -->
```

**문제점**:
- 텍스트 크기만 3개 (실제로는 최소 6-8개 필요)
- Spacing 시스템 부재 (margin, padding)
- Corner radius 1개만 정의 (여러 크기 필요)
- 아이콘 크기 미정의
- 높이(height) 값 미정의

#### 2. **themes.xml의 하드코딩된 값들**
```xml
<!-- 하드코딩 예시 -->
<item name="android:layout_height">50dp</item>              <!-- 버튼 높이 -->
<item name="cardCornerRadius">20dp</item>                   <!-- 카드 corner -->
<item name="android:layout_marginStart">20dp</item>         <!-- margin -->
<item name="android:paddingStart">15.35dp</item>            <!-- 비표준 padding -->
<item name="android:paddingTop">21dp</item>                 <!-- 비표준 padding -->
<item name="cornerRadius">15dp</item>                       <!-- button corner -->
<item name="strokeWidth">1dp</item>                         <!-- 테두리 -->
<item name="boxCornerRadiusTopStart">12dp</item>            <!-- textinput corner -->
```

**문제점**: 중복된 값, 비표준 값, 유지보수 어려움

#### 3. **Typography 정보 분산**
- `themes.xml`에서 TextAppearance 정의
- 실제 폰트, 줄 높이(line-height) 정보 미정의
- 글자 두께(font-weight) 미정의

#### 4. **Color Selector 미정의**
themes.xml에서 참조하는 것들:
```xml
<item name="backgroundTint">@color/selector_button_primary_bg</item>
<item name="strokeColor">@color/selector_button_secondary_stroke</item>
```
실제로 `selector_button_*` 파일들이 존재해야 하는데 불명확

---

## 주요 문제점

### 1. **색상 시스템의 WCAG 접근성 미충족**
- `color_secondary` (#CCC2DC): 흰색 텍스트와 대비율 1.9:1 (WCAG AA 4.5:1 미충족)
- `color_text_sub` (#696969): 14px 미만 소폰트에서 가독성 위험 (대비율 4.5:1)
- `color_button_secondary_text` (#696969): 대비 부족
- **영향**: 장애인 사용자 불편, 법적 리스크

### 2. **색상 계통 혼동**
- `color_primary` (#1852FF) + `color_info` (#3B82F6): 모두 파란색 계열
  - 사용자가 "브랜드 버튼"과 "정보 안내"를 구별 못함
  - 헬스케어 앱에서 특히 위험 (약물 안내 vs 주요 액션)
- `color_secondary` (#CCC2DC): Primary와 색상 계통이 다름 → 브랜드 통일성 저하

### 3. **Gray Scale 스케일 역전 오류**
- `gray_600` (#666666) > `gray_700` (#777777) 
- 숫자가 올라갈수록 어두워야 하는 원칙 위반
- 다크모드 및 향후 컴포넌트 작업 시 버그 유발 가능

### 4. **Spacing 시스템 부재**
- Margin, Padding 값이 일관성 없음
- 8dp 기반 scale이 아님 (15.35dp, 21dp 등 비표준)
- 컴포넌트 간 간격 규칙 부재

### 5. **Corner Radius 비표준**
- 15dp, 20dp, 12dp 혼용
- 체계적인 radius scale 부재

### 6. **Typography 불완전**
- 텍스트 크기만 정의 (font-family, line-height, letter-spacing 미정의)
- 새로운 텍스트 스타일 추가 시 하드코딩 위험

### 7. **Color Reference 불명확**
- colors.xml에 정의된 색상과 themes.xml 사용이 연결되지 않음
- selector 파일 구조 불명확

### 8. **불필요한 색상 중복**
- `blue_primary` (#2563EB): `color_primary`와 역할 중복
- `alias: primary` (#1852FF): `color_primary`와 완전히 동일
- `orange_accent` (#FF6B35): 역할 미정의

### 9. **유지보수성 저하**
- XML에 하드코딩된 값 → 변경 시 여러 곳 수정 필요
- 디바이스별 다양한 화면 크기 대응 미흡

---

## 개선 방향

### 원칙
1. **8dp 기반 Spacing Scale** 도입
   - 8, 16, 24, 32, 40, 48, 56, 64dp

2. **8dp 기반 Radius Scale** 통일
   - 4, 8, 12, 16, 20, 24, 28dp

3. **Typography System 완성**
   - 크기, 자중, 줄 높이 모두 정의

4. **Component Token 도입**
   - 각 컴포넌트별 기본 스타일 토큰

5. **Color System 강화**
   - 의도(intent)에 따른 색상 분류

---

## 구체적 개선안

### 1. **dimens.xml 전체 재구성**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ============= Typography ============= -->
    <!-- Label / Caption -->
    <dimen name="text_size_label">12sp</dimen>
    <dimen name="text_size_caption">12sp</dimen>
    
    <!-- Body / Body Medium -->
    <dimen name="text_size_body">15sp</dimen>
    <dimen name="text_size_body_medium">14sp</dimen>
    <dimen name="text_size_body_small">13sp</dimen>
    
    <!-- Headline / Title -->
    <dimen name="text_size_headline">18sp</dimen>
    <dimen name="text_size_title">20sp</dimen>
    <dimen name="text_size_title_large">24sp</dimen>
    <dimen name="text_size_display">32sp</dimen>
    
    <!-- Line Height (선택: 줄 높이가 필요한 경우) -->
    <!-- 일반적으로 text_size * 1.5 -->
    
    <!-- ============= Spacing (8dp Scale) ============= -->
    <dimen name="spacing_xs">4dp</dimen>       <!-- 4dp -->
    <dimen name="spacing_sm">8dp</dimen>       <!-- 8dp -->
    <dimen name="spacing_md">16dp</dimen>      <!-- 16dp -->
    <dimen name="spacing_lg">24dp</dimen>      <!-- 24dp -->
    <dimen name="spacing_xl">32dp</dimen>      <!-- 32dp -->
    <dimen name="spacing_2xl">40dp</dimen>     <!-- 40dp -->
    <dimen name="spacing_3xl">48dp</dimen>     <!-- 48dp -->
    <dimen name="spacing_4xl">56dp</dimen>     <!-- 56dp -->
    
    <!-- ============= Component Heights ============= -->
    <dimen name="button_height">50dp</dimen>
    <dimen name="button_height_small">40dp</dimen>
    <dimen name="button_height_large">56dp</dimen>
    <dimen name="textinput_height">48dp</dimen>
    
    <!-- ============= Component Padding/Margin ============= -->
    <!-- 버튼 내부 padding -->
    <dimen name="button_padding_horizontal">16dp</dimen>
    <dimen name="button_padding_vertical">12dp</dimen>
    
    <!-- 카드 padding -->
    <dimen name="card_padding">16dp</dimen>
    <dimen name="card_padding_vertical">20dp</dimen>
    <dimen name="card_padding_horizontal">16dp</dimen>
    
    <!-- 화면 여백 -->
    <dimen name="screen_margin_horizontal">20dp</dimen>
    <dimen name="screen_margin_vertical">16dp</dimen>
    
    <!-- ============= Corner Radius ============= -->
    <dimen name="corner_xs">4dp</dimen>        <!-- 작은 버튼, input -->
    <dimen name="corner_sm">8dp</dimen>        <!-- 버튼, chips -->
    <dimen name="corner_md">12dp</dimen>       <!-- textinput, 일반 -->
    <dimen name="corner_lg">16dp</dimen>       <!-- 카드 -->
    <dimen name="corner_xl">20dp</dimen>       <!-- 큰 카드 -->
    <dimen name="corner_2xl">24dp</dimen>      <!-- 매우 큰 카드 -->
    
    <!-- ============= Border & Stroke ============= -->
    <dimen name="stroke_thin">1dp</dimen>
    <dimen name="stroke_medium">2dp</dimen>
    
    <!-- ============= Icon Size ============= -->
    <dimen name="icon_size_xs">16dp</dimen>
    <dimen name="icon_size_sm">20dp</dimen>
    <dimen name="icon_size_md">24dp</dimen>
    <dimen name="icon_size_lg">32dp</dimen>
    <dimen name="icon_size_xl">48dp</dimen>
    
    <!-- ============= Divider ============= -->
    <dimen name="divider_height">1dp</dimen>
    
    <!-- ============= Legacy/Deprecated (마이그레이션 중) ============= -->
    <!-- 기존 코드와의 호환성을 위해 잠시 유지 -->
    <dimen name="chat_corner">15dp</dimen>     <!-- 사용 중단 권장 -->
</resources>
```

### 2. **colors.xml 개선** (docs/color-palette-improvements.md 반영)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ============= Base Colors ============= -->
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>

    <!-- ============= Brand Colors ============= -->
    <!-- Primary: 유지 (신뢰감, 전문성, 채도 높음) -->
    <color name="color_primary">#1852FF</color>
    <color name="color_on_primary">#FFFFFF</color>
    
    <!-- Primary Container: 추가 (선택 상태, 활성 배경) -->
    <color name="color_primary_container">#E8F0FF</color>
    
    <!-- Secondary: 교체 (기존 #CCC2DC → #1244CC) -->
    <!-- 이유: 기존은 WCAG 대비율 1.9:1 미충족. Primary 딥버전으로 변경해 계통 통일 -->
    <color name="color_secondary">#1244CC</color>
    <color name="color_on_secondary">#FFFFFF</color>

    <!-- ============= Semantic Colors (Intent-based) ============= -->
    <!-- Success: 유지 (건강 달성, 복약 완료, 정상 수치) -->
    <color name="color_success">#10B981</color>
    <color name="color_success_light">#D1FAE5</color>
    
    <!-- Error: 유지 (이상 수치, 긴급 알림, 입력 오류) -->
    <color name="color_error">#EF4444</color>
    <color name="color_error_light">#FEE2E2</color>
    
    <!-- Warning: 유지 (경계 수치, 복약 지연, 주의 상태) -->
    <color name="color_warning">#F59E0B</color>
    <color name="color_warning_light">#FEF3C7</color>
    
    <!-- Info: 교체 (기존 #3B82F6 → #0EA5E9) -->
    <!-- 이유: Primary와 모두 파란색이라 구별 불가. 청록으로 분리 -->
    <color name="color_info">#0EA5E9</color>
    <color name="color_info_light">#E0F2FE</color>

    <!-- ============= Text Colors ============= -->
    <color name="color_text_main">#2A2A2A</color>        <!-- 기본 텍스트, 대비율 17.1:1 -->
    
    <!-- color_text_sub: 교체 (기존 #696969 → #555555) -->
    <!-- 이유: 14px 미만 소폰트에서 가독성 위험. 대비율 강화 -->
    <color name="color_text_sub">#555555</color>
    <color name="color_text_hint">#A0A0A0</color>        <!-- Placeholder, 부가 설명 -->
    <color name="color_text_disabled">#CCCCCC</color>    <!-- 비활성 텍스트 -->

    <!-- ============= Background / Surface ============= -->
    <color name="color_background">#F4F4F4</color>       <!-- 레이어 0: 기본 배경 -->
    <color name="color_surface">#FFFFFF</color>          <!-- 레이어 1: 카드, 리스트 -->
    <color name="color_surface_raised">#FAFCFF</color>   <!-- 레이어 2: 모달, 드롭다운 -->

    <!-- ============= Button Colors ============= -->
    <!-- Primary Button: 유지 -->
    <color name="color_button_primary_bg">#1852FF</color>
    <color name="color_button_primary_text">#FFFFFF</color>
    <color name="color_button_primary_bg_disabled">#CCCCCC</color>
    <color name="color_button_primary_text_disabled">#FFFFFF</color>
    
    <!-- Secondary Button: text 교체 (대비 강화) -->
    <color name="color_button_secondary_bg">#FFFFFF</color>
    <color name="color_button_secondary_text">#555555</color>  <!-- 기존 #696969 → #555555 -->
    <color name="color_button_secondary_bg_disabled">#F0F0F0</color>
    <color name="color_button_secondary_text_disabled">#B0B0B0</color>
    <color name="color_button_secondary_stroke">#E0E0E0</color>

    <!-- ============= Gray Tones (Neutral) ============= -->
    <color name="gray_50">#FAFAFA</color>
    <color name="gray_100">#F5F5F5</color>
    <color name="gray_200">#EEEEEE</color>
    <color name="gray_300">#E0E0E0</color>
    <color name="gray_400">#BDBDBD</color>
    <color name="gray_500">#9E9E9E</color>
    <!-- gray_600/700: 순서 정정 (기존 역전 오류 수정) -->
    <color name="gray_600">#757575</color>   <!-- 기존 #666666 → #757575 -->
    <color name="gray_700">#616161</color>   <!-- 기존 #777777 → #616161 -->
    <color name="gray_800">#424242</color>
    <color name="gray_900">#212121</color>

    <!-- ============= Deprecated / Pending Deletion ============= -->
    <!-- blue_primary: 삭제 권장 (color_primary와 역할 중복) -->
    <!-- alias "primary": 삭제 권장 (color_primary와 완전히 동일) -->
    <!-- orange_accent: 보류 (역할 정의 후 이름 변경 또는 삭제) -->
    
    <!-- ============= Legacy Alias ============= -->
    <color name="color_button_gray">@color/gray_400</color>
    <color name="color_button_pressed_bg">#E5E5E5</color>
</resources>
```

**주요 변경사항 요약**:
- ⚠️ `color_secondary`: #CCC2DC → #1244CC (WCAG 대비율 개선)
- ⚠️ `color_info`: #3B82F6 → #0EA5E9 (Primary와 분리)
- ⚠️ `color_text_sub`: #696969 → #555555 (대비 강화)
- ⚠️ `color_button_secondary_text`: #696969 → #555555 (대비 강화)
- ⚠️ `gray_600`: #666666 → #757575 (스케일 역전 수정)
- ⚠️ `gray_700`: #777777 → #616161 (스케일 역전 수정)
- ✅ `color_primary_container`: #E8F0FF (추가)
- ✅ `color_surface_raised`: #FAFCFF (추가)
- 🗑️ `blue_primary`, `alias: primary`, `orange_accent`: 삭제 또는 역할 정의 필요

### 3. **Typography 전용 파일 추가**

새 파일: `app/src/main/res/values/typography.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ============= Display ============= -->
    <style name="TextAppearance.HelloDoctor.Display" parent="@android:style/TextAppearance">
        <item name="android:textSize">@dimen/text_size_display</item>
        <item name="android:textColor">@color/color_text_main</item>
        <item name="android:fontFamily">@font/font_family_main</item>
        <item name="android:textStyle">bold</item>
    </style>

    <!-- ============= Heading / Title ============= -->
    <style name="TextAppearance.HelloDoctor.Title" parent="@android:style/TextAppearance">
        <item name="android:textSize">@dimen/text_size_title</item>
        <item name="android:textColor">@color/color_text_main</item>
        <item name="android:fontFamily">@font/font_family_main</item>
        <item name="android:textStyle">bold</item>
    </style>

    <style name="TextAppearance.HelloDoctor.Headline" parent="@android:style/TextAppearance">
        <item name="android:textSize">@dimen/text_size_headline</item>
        <item name="android:textColor">@color/color_text_main</item>
        <item name="android:fontFamily">@font/font_family_main</item>
        <item name="android:textStyle">bold</item>
    </style>

    <!-- ============= Body ============= -->
    <style name="TextAppearance.HelloDoctor.Body" parent="@android:style/TextAppearance">
        <item name="android:textSize">@dimen/text_size_body</item>
        <item name="android:textColor">@color/color_text_main</item>
        <item name="android:fontFamily">@font/font_family_main</item>
    </style>

    <style name="TextAppearance.HelloDoctor.BodyMedium" parent="TextAppearance.HelloDoctor.Body">
        <item name="android:textSize">@dimen/text_size_body_medium</item>
    </style>

    <style name="TextAppearance.HelloDoctor.BodySmall" parent="TextAppearance.HelloDoctor.Body">
        <item name="android:textSize">@dimen/text_size_body_small</item>
    </style>

    <!-- ============= Label / Caption ============= -->
    <style name="TextAppearance.HelloDoctor.Label" parent="@android:style/TextAppearance">
        <item name="android:textSize">@dimen/text_size_label</item>
        <item name="android:textColor">@color/color_text_sub</item>
        <item name="android:fontFamily">@font/font_family_main</item>
    </style>

    <style name="TextAppearance.HelloDoctor.Caption" parent="@android:style/TextAppearance">
        <item name="android:textSize">@dimen/text_size_caption</item>
        <item name="android:textColor">@color/color_text_sub</item>
        <item name="android:fontFamily">@font/font_family_main</item>
    </style>
</resources>
```

### 4. **색상 Selector 파일 정리**

`colors.xml`에 selector 정의 추가 또는 별도 파일:

```xml
<!-- app/src/main/res/color/selector_button_primary_bg.xml -->
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/color_button_primary_bg_disabled" android:state_enabled="false"/>
    <item android:color="@color/color_button_primary_bg"/>
</selector>

<!-- app/src/main/res/color/selector_button_secondary_stroke.xml -->
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/gray_300" android:state_enabled="false"/>
    <item android:color="@color/color_button_secondary_stroke"/>
</selector>
```

### 5. **themes.xml 하드코딩 값 제거**

예시 수정:

```xml
<!-- Before: 하드코딩 -->
<style name="Widget.HelloDoctor.Button">
    <item name="android:layout_height">50dp</item>
    <item name="android:layout_marginStart">20dp</item>
    <item name="android:layout_marginEnd">20dp</item>
    <item name="cornerRadius">15dp</item>
</style>

<!-- After: dimens 참조 -->
<style name="Widget.HelloDoctor.Button">
    <item name="android:layout_height">@dimen/button_height</item>
    <item name="android:layout_marginStart">@dimen/screen_margin_horizontal</item>
    <item name="android:layout_marginEnd">@dimen/screen_margin_horizontal</item>
    <item name="cornerRadius">@dimen/corner_lg</item>
</style>
```

---

## 구현 로드맵

### Phase 1: 색상 팔레트 개선 (즉시) ✨ 우선순위 HIGH
**이유**: WCAG 접근성 문제, 색상 계통 혼동, 스케일 역전 오류 즉시 해결 필요

#### 1-1. colors.xml 교체 (6개 항목)
```bash
# 영향받는 파일들 검색 (사전 분석)
grep -r "color_secondary" app/src/
grep -r "color_info" app/src/
grep -r "color_text_sub" app/src/
grep -r "gray_600\|gray_700" app/src/
```

**변경 사항**:
- `color_secondary`: #CCC2DC → #1244CC (대비율 개선)
- `color_info`: #3B82F6 → #0EA5E9 (Primary와 분리)
- `color_text_sub`: #696969 → #555555 (대비 강화)
- `color_button_secondary_text`: #696969 → #555555
- `gray_600`: #666666 → #757575 (스케일 정정)
- `gray_700`: #777777 → #616161 (스케일 정정)

**추가 항목** (2개):
- `color_primary_container`: #E8F0FF (선택 상태, 활성 배경)
- `color_surface_raised`: #FAFCFF (모달, 드롭다운 elevation)

**삭제 권장** (3개):
- `blue_primary` (#2563EB): color_primary와 역할 중복
- `alias: primary`: color_primary와 완전히 동일
- `orange_accent` (#FF6B35): 역할 미정의 (향후 결정 필요)

**산출물**: `app/src/main/res/values/colors.xml` (완전 교체)

#### 1-2. 영향 범위 분석 및 적용
- [ ] 테마 자동 적용되는지 검증 (색상 의존 컴포넌트 확인)
- [ ] 스크린샷 비교 (UI 시각적 변화 확인)
- [ ] 접근성 검사 (WCAG 대비율 재검증)

**테스트 항목**:
- Primary / Secondary 버튼 시각 확인
- 정보 안내 vs 주요 액션 구분 가능 여부
- Gray 스케일 순서 시각 확인

### Phase 2: 기반 시스템 정의 (1주일)
**이유**: Phase 1 색상 변경이 안정화된 후 나머지 시스템 정리

#### 2-1. dimens.xml 전체 재구성
- [ ] Spacing scale (8dp base): 4, 8, 16, 24, 32, 40, 48, 56dp
- [ ] Component heights: button, textinput, card padding
- [ ] Corner radius scale: 4, 8, 12, 16, 20, 24dp (일관성)
- [ ] Icon sizes: 16, 20, 24, 32, 48dp
- [ ] themes.xml의 하드코딩 값 → dimens 참조로 변경

**산출물**: `app/src/main/res/values/dimens.xml` (개선)

#### 2-2. typography.xml 신규 생성
- [ ] Display, Title, Headline, Body, Label, Caption 스타일
- [ ] font-family, font-size, font-weight 정의
- [ ] line-height 정의 (선택, 가능하면 포함)

**산출물**: `app/src/main/res/values/typography.xml` (신규)

#### 2-3. Color Selector 정리
- [ ] `app/src/main/res/color/selector_button_primary_bg.xml` 등
- [ ] selector 파일 구조 명확화

**산출물**: `app/src/main/res/color/*.xml` (selector files)

### Phase 3: 스타일 정리 (2주)
- [ ] `themes.xml` 정리: 하드코딩 값 제거, dimens 참조
- [ ] 모든 styles에서 spacing 일관성 확보
- [ ] 새로운 컴포넌트 스타일 추가 (Dialog, Chip, Badge, Snackbar 등)
- [ ] 기존 컴포넌트 styles 검수 및 정규화

### Phase 4: Compose 연동 (향후, 3주+)
- [ ] Material3 Theme 정의
- [ ] XML 값과 Compose 값 동기화
- [ ] Compose Preview 작성
- [ ] 완전 Compose 전환 계획 수립

### Phase 5: 문서화 (지속적)
- [ ] Design System 가이드 작성
- [ ] 개발자 온보딩 자료 작성
- [ ] 색상/크기 스케일 문서화
- [ ] 사용 예제 추가

---

## 기대 효과

### 접근성 개선 (즉시, Phase 1)
- ✅ WCAG AA 준수 → 법적 리스크 감소
- ✅ 시각장애인 사용자 편의성 향상
- ✅ 모든 사용자를 위한 명확한 정보 전달

### 설계 일관성 (Phase 1-2)
- ✅ 색상 계통 통일 → 사용자 혼동 감소
- ✅ 브랜드 정체성 강화
- ✅ 헬스케어 앱 특성에 맞는 신뢰감 향상

### 개발 효율성 (Phase 2-3)
- ✅ 하드코딩 제거 → 유지보수성 향상
- ✅ 일관된 spacing → 레이아웃 예측 가능
- ✅ 재사용 가능한 스타일 → 개발 속도 증가

### 장기 이점 (Phase 4+)
- ✅ 중앙화된 정의 → 변경 용이
- ✅ Compose 대응 → 향후 마이그레이션 용이
- ✅ 버전 관리 용이 → 디자인 버전 추적

---

## 문제별 해결책

| 문제 | Phase | 해결책 |
|------|-------|--------|
| WCAG 대비율 미충족 | 1 | 색상 교체 (secondary, info, text_sub) |
| 색상 계통 혼동 | 1 | Primary/Info 분리 (#1852FF vs #0EA5E9) |
| Gray scale 역전 | 1 | 순서 정정 (gray_600/700) |
| 하드코딩 값 | 2 | dimens.xml 확장 + themes.xml 참조 변경 |
| Spacing 불일관 | 2 | 8dp scale 도입 |
| Typography 분산 | 2 | typography.xml 신규 생성 |
| 불필요한 색상 | 1-2 | blue_primary, alias 삭제 |
| Compose 미지원 | 4 | Material3 Theme 정의 |

---

## 참고 자료

### Material Design 3 System
- Spacing scale: 8dp base
- Type scale: 6-8개 크기 정의
- Color system: Semantic colors + tones
- Elevation: Surface + raised levels

### 헬스케어 앱 설계 고려사항
- 신뢰감 색상 (blue, green)
- 긴급 상황 색상 (red) 신중한 사용
- 복약 정보 vs 주요 액션 명확한 구분
- 접근성 (WCAG AA 필수)

### 참고 문서
- `docs/color-palette-improvements.md` — 색상 팔레트 상세 분석
- Material Design 3: m3.material.io/foundations/color

---

## 실행 계획

### Week 1 (Phase 1: 색상 개선)
```bash
# 1. colors.xml 수정 (6 교체 + 2 추가 + 3 삭제)
# 2. 영향받는 파일 검색 및 테스트
grep -r "color_secondary\|color_info\|gray_600\|gray_700" app/src/

# 3. 빌드 및 시각 검증
./gradlew build
# 스크린샷: Primary/Secondary 버튼, 정보 안내 UI 검증

# 4. 접근성 검사
# Android Accessibility Scanner 실행

# 5. PR 생성 및 리뷰
# Title: "feat(design): WCAG 색상 팔레트 개선 및 스케일 정정"
```

### Week 2-3 (Phase 2: Spacing/Typography)
```bash
# 1. dimens.xml 확장 (spacing, sizes, radius)
# 2. typography.xml 신규 생성
# 3. themes.xml 하드코딩 제거

# 4. PR 단위 진행 (각각 별도)
# - PR 1: dimens.xml 확장
# - PR 2: typography.xml 신규
# - PR 3: themes.xml 정리
```

### Week 4+ (Phase 3-4)
- 스타일 정리 및 Compose 준비

---

## 의견 (Opinion)

**현재 상태**:
- 색상 시스템은 기본은 있으나 접근성 문제와 혼동 가능성 높음
- Spacing/Typography 시스템은 거의 부재한 상태
- 하드코딩된 값들이 산재되어 유지보수 어려움

**개선 우선순위**:
1. **CRITICAL (Phase 1)**: 색상 팔레트 (WCAG 접근성 + 색상 혼동)
2. **HIGH (Phase 2)**: Spacing 시스템 (하드코딩 제거, 일관성)
3. **MEDIUM (Phase 2)**: Typography (새 스타일 추가 시 오류 방지)
4. **LOW (Phase 3+)**: Compose 연동 (장기 계획)

**권장사항**:
- ⚠️ **Phase 1을 최우선으로 완료** (접근성 + 사용성 개선)
  - colors.xml 교체만으로도 많은 문제 해결
  - 검증 후 즉시 merge 가능
  
- Phase 2는 PR 단위로 컴포넌트별 진행 (작은 단위의 변경)
  - 각 PR마다 빌드 + 스크린샷 검증
  - 기존 코드에 영향 최소화
  
- Phase 3는 시간 여유가 있을 때 진행
  
- Phase 4는 Compose 도입 시점에 계획 (현재는 검토만)

