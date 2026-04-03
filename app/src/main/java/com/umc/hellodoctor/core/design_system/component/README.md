# HelloDoctor Compose Components

HelloDoctor 앱의 Compose 기반 UI 컴포넌트 라이브러리입니다.

## 📁 구조

```
component/
├── button/
│   ├── PrimaryButton.kt        # 주요 액션 버튼
│   └── SecondaryButton.kt       # 보조 버튼 (아웃라인)
├── card/                        # 카드 컴포넌트 (향후)
├── input/                       # 입력 필드 (향후)
├── dialog/                      # 다이얼로그 (향후)
├── text/                        # 텍스트 스타일 (향후)
└── README.md
```

## 🎨 사용 방법

### Button

#### PrimaryButton (주요 액션)

```kotlin
import com.umc.hellodoctor.core.design_system.component.button.PrimaryButton

PrimaryButton(
    text = "로그인",
    onClick = { viewModel.login() },
    enabled = isFormValid
)
```

**특징:**
- 파란색 배경 (Primary color)
- 높이: 50dp (ButtonHeight)
- 좌우 여백: 20dp (Spacing20)
- 모서리: 15dp (CornerLarge)
- 상태: enabled/disabled

#### SecondaryButton (보조 액션)

```kotlin
import com.umc.hellodoctor.core.design_system.component.button.SecondaryButton

SecondaryButton(
    text = "취소",
    onClick = { navController.popBackStack() }
)
```

**특징:**
- 흰색 배경 + 1dp 테두리
- 높이: 50dp (ButtonHeight)
- 좌우 여백: 20dp (Spacing20)
- 모서리: 15dp (CornerLarge)
- 상태: enabled/disabled

### ComposeView에서 사용

Fragment의 XML 레이아웃에서 ComposeView 사용:

```xml
<!-- fragment_drug_search.xml -->
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/composeView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Fragment 코드:

```kotlin
class DrugSearchFragment : Fragment() {
    private val viewModel: DrugViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.composeView.setContent {
            HelloDoctorTheme {
                DrugSearchScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun DrugSearchScreen(viewModel: DrugViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HelloDoctorDimensions.Spacing16)
    ) {
        // 검색 입력
        TextField(
            value = viewModel.inputText.value ?: "",
            onValueChange = { viewModel.updateInputText(it) }
        )

        Spacer(modifier = Modifier.height(HelloDoctorDimensions.Spacing12))

        // 버튼
        PrimaryButton(
            text = "검색",
            onClick = { viewModel.search() }
        )

        Spacer(modifier = Modifier.height(HelloDoctorDimensions.Spacing16))

        // 결과 리스트
        LazyColumn {
            items(viewModel.searchResults.value ?: emptyList()) { medicine ->
                MedicineItem(medicine = medicine)
            }
        }
    }
}
```

## 🎭 Theme 적용

모든 Compose 콘텐츠는 `HelloDoctorTheme`으로 감싸야 합니다:

```kotlin
binding.composeView.setContent {
    HelloDoctorTheme {
        MyScreen()
    }
}
```

**Theme에 포함된 것:**
- Material3 Color Scheme (Light/Dark)
- Typography (Display, Headline, Title, Body, Label)
- Shapes (Corner radius)

## 🎨 토큰 활용

### 색상

```kotlin
import com.umc.hellodoctor.core.design_system.token.HelloDoctorColors

Text(
    text = "에러 메시지",
    color = HelloDoctorColors.Error
)

Box(
    modifier = Modifier
        .background(HelloDoctorColors.Background)
        .padding(HelloDoctorDimensions.Spacing16)
)
```

### 간격 (Spacing)

```kotlin
import com.umc.hellodoctor.core.design_system.token.HelloDoctorDimensions

Column(
    modifier = Modifier
        .padding(HelloDoctorDimensions.Spacing16)
        .fillMaxWidth()
) {
    Text("제목")
    Spacer(modifier = Modifier.height(HelloDoctorDimensions.Spacing12))
    Text("내용")
}
```

### 타이포그래피

```kotlin
import com.umc.hellodoctor.core.design_system.token.HelloDoctorDimensions

Text(
    text = "헤드라인",
    fontSize = HelloDoctorDimensions.TextSizeHeadline,
    fontWeight = FontWeight.Bold
)
```

## 📝 컴포넌트 추가 가이드

새 컴포넌트를 추가할 때:

1. **파일 생성**: `component/[category]/[ComponentName].kt`
   ```kotlin
   package com.umc.hellodoctor.core.design_system.component.[category]
   ```

2. **토큰 사용**: HelloDoctorColors, HelloDoctorDimensions 사용
   ```kotlin
   import com.umc.hellodoctor.core.design_system.token.*
   ```

3. **Composable 정의**:
   ```kotlin
   @Composable
   fun MyComponent(
       // 파라미터
   ) {
       // 구현
   }
   ```

4. **Preview 작성**: 테스트와 문서화용
   ```kotlin
   @Preview(showBackground = true)
   @Composable
   private fun MyComponentPreview() {
       HelloDoctorTheme {
           MyComponent()
       }
   }
   ```

5. **문서화**: 주석에 사용 방법 기술
   ```kotlin
   /**
    * 컴포넌트 설명
    *
    * @param param1 파라미터 설명
    *
    * 사용 예:
    * ```kotlin
    * MyComponent(...)
    * ```
    */
   ```

## 🧪 Preview 보기

Android Studio에서 `@Preview` 함수를 마우스 우측 클릭하면 "Show Compose Preview" 옵션이 나타납니다.

또는 코드 오른쪽의 Preview 아이콘을 클릭하세요.

## 🔗 관련 문서

- [Design System 구조 계획](../../docs/DESIGN_SYSTEM_COMPOSE_STRUCTURE.md)
- [Token 정의](../token/Color.kt)
- [Theme 정의](../theme/HelloDoctorTheme.kt)

## 📋 확장 계획

**Phase 1 (현재)**: Button 컴포넌트 ✅

**Phase 2 (진행 중)**:
- [ ] Card 컴포넌트 (BaseCard, HealthCard)
- [ ] Input 컴포넌트 (TextField, OutlinedTextField)
- [ ] Dialog 컴포넌트 (ConfirmDialog)

**Phase 3**:
- [ ] List/LazyColumn 아이템
- [ ] Tab/Navigation 컴포넌트
- [ ] Slider/Picker 컴포넌트
- [ ] Toast/Snackbar 컴포넌트

---

**마지막 업데이트**: 2026-04-03
