# 코딩 컨벤션

## 개요

HelloDoctor-android는 **Kotlin 코딩 컨벤션**과 **Android 베스트 프랙티스**를 따릅니다.

---

## 1. 네이밍 규칙

### 1.1 패키지명
- 소문자 + 언더스코어 금지
- 계층별로 분리: `feature.{기능명}.{계층}`

```kotlin
// ✅ 올바름
package umc.hellodoctor.feature.auth.presentation
package umc.hellodoctor.feature.auth.domain.usecase
package umc.hellodoctor.feature.auth.data.remote

// ❌ 잘못됨
package umc.hellodoctor.feature.auth_presentation
package umc.hellodoctor.AuthPresentationModule
```

### 1.2 클래스명
- **PascalCase** (UpperCamelCase)
- 명사 사용

```kotlin
// ✅ 올바름
class LoginViewModel
data class AuthUser
interface AuthRepository
object AppConstants

// ❌ 잘못됨
class login_view_model
class AuthUserData
interface IAuthRepository  // Interface 접두사 불필요
```

#### 클래스명 규칙

| 패턴 | 예시 | 규칙 |
|------|------|------|
| ViewModel | `LoginViewModel` | `{화면명}ViewModel` |
| UseCase | `LoginUseCase` | `{동작명}UseCase` |
| Repository | `AuthRepository` | `{기능명}Repository` (인터페이스) |
| RepositoryImpl | `AuthRepositoryImpl` | `{기능명}RepositoryImpl` |
| DataSource | `AuthRemoteDataSource` | `{기능명}{Local/Remote}DataSource` |
| API Interface | `AuthApi` | `{기능명}Api` |
| DAO | `UserDao` | `{모델명}Dao` |
| Entity | `UserEntity` | `{모델명}Entity` |
| DTO | `LoginResponse` | `{동작명}{Response/Request}` |
| Mapper | `AuthMapper` | `{기능명}Mapper` |
| State | `LoginUiState` | `{화면명}UiState` |
| Event | `LoginEvent` | `{화면명}Event` |

### 1.3 함수명
- **camelCase** (lowerCamelCase)
- 동사로 시작 (get, set, calculate, validate, etc.)

```kotlin
// ✅ 올바름
fun login(email: String, password: String)
fun validateEmail(email: String): Boolean
fun onLoginClicked()
fun getUserFromDatabase()

// ❌ 잘못됨
fun Login()
fun email_validation()
fun loginUserWithEmailAndPassword()  // 너무 길음
```

**콜백**: `on` 접두사 필수 (`onLoginClicked`, `onSuccess`, `onError`)

### 1.4 변수명
- **camelCase**
- 명사 사용
- 한 글자 변수명 피함 (루프 제외: i, j, k)

```kotlin
// ✅ 올바름
val userEmail: String
var isLoading: Boolean
private val authRepository: AuthRepository

// ❌ 잘못됨
val user_email: String
var isLoad: Boolean
val repo: AuthRepository  // 약자 피함
```

#### Private 변수 vs Public 변수
```kotlin
// ✅ Private 변수는 underscore로 시작 (StateFlow/LiveData 패턴)
private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

// ❌ 잘못됨
private val uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
```

### 1.5 상수명
- **UPPER_SNAKE_CASE**
- `companion object` 또는 `object` 내부에 정의

```kotlin
// ✅ 올바름
companion object {
    const val DEFAULT_TIMEOUT_MS = 5000
    const val MAX_RETRY_COUNT = 3
}

object AppConstants {
    const val BASE_URL = "https://api.example.com"
    const val API_VERSION = "v1"
}

// ❌ 잘못됨
const val defaultTimeoutMs = 5000
val MAX_RETRY_COUNT = 3  // const 누락
```

---

## 2. 형식 (Formatting)

### 2.1 들여쓰기
- **4칸 스페이스** (탭 금지)
- IDE 자동 포맷팅 사용

```kotlin
// ✅ 올바름
fun login(email: String, password: String) {
    viewModelScope.launch {
        val result = loginUseCase(email, password)
        _uiState.value = result
    }
}
```

### 2.2 줄 길이
- 최대 **120 글자**
- IDE 설정: `Editor > Code Style > Kotlin > Line length` → 120

```kotlin
// ✅ 올바름 (한 줄로 읽기 쉬운 길이)
val user = authRepository.login(email, password)

// ⚠️ 길지만 필요한 경우 줄 바꿈
val response = authRepository.login(
    email = email,
    password = password,
    rememberMe = true
)

// ❌ 잘못됨 (150글자 초과)
val response = authRepository.loginWithEmailAndPasswordAndRememberMeAndAutoFillAndPreferencesAndNotificationSettings()
```

### 2.3 블록 스타일
```kotlin
// ✅ 올바름 - 한 줄 if는 괄호 생략 가능
if (isValid) return

// ✅ 올바름 - 여러 줄은 괄호 필수
if (isValid) {
    processData()
    return
}

// ❌ 잘못됨
if (isValid)
    processData()
    return

// ✅ 올바름 - When 표현식
val message = when (result) {
    is Success -> "성공"
    is Error -> "실패"
}

// ❌ 잘못됨
when (result) {
    is Success -> {
        return "성공"
    }
    is Error -> {
        return "실패"
    }
}
```

---

## 3. 타입과 Null Safety

### 3.1 Null 안전성

Non-null 타입 선호, Nullable 타입은 명시적으로 (`String?`). `!!` 최소화 (NPE 위험).

### 3.2 타입 별칭 (Type Alias)
```kotlin
// ✅ 올바름 - 복잡한 타입은 별칭으로
typealias UserCallback = (user: AuthUser) -> Unit
typealias ErrorHandler = (exception: Exception) -> Unit

fun setUserListener(callback: UserCallback) {
    // ...
}

// ❌ 잘못됨
fun setUserListener(callback: (user: AuthUser) -> Unit) {
    // ... 중복되는 타입 정의
}
```

---

## 4. 클래스 및 함수 구조

### 4.1 클래스 멤버 순서

1. 생성자 주입
2. companion object
3. Private properties
4. Public properties
5. Lifecycle methods
6. Public methods
7. Private methods
8. Inner classes/enums

### 4.2 함수 파라미터
```kotlin
// ✅ 올바름 - 3개 이상은 여러 줄
fun login(
    email: String,
    password: String,
    rememberMe: Boolean = false
) {
    // ...
}

// ✅ 올바름 - Named parameter 사용
login(
    email = "user@example.com",
    password = "password",
    rememberMe = true
)

// ❌ 잘못됨 - 한 줄에 너무 많음
fun login(email: String, password: String, rememberMe: Boolean = false, autoFill: Boolean = false, ...) {
}
```

### 4.3 수정자 순서 (Modifiers)
```kotlin
// ✅ 올바름
public final data class User(...)
private suspend fun loadData() {}
internal sealed class Result {}

// 순서: public → protected → private
//      final → open
//      suspend → inline
```

---

## 5. String과 Interpolation

### 5.1 String Interpolation

`$변수` 또는 `${식}` 사용, `+` 연결 피하기. 복잡한 식은 변수로 분리.

### 5.2 String Resources (XML)

사용자 보이는 문자열은 `res/values/strings.xml`에 정의, 하드코딩 금지.

---

## 6. 접근 제한자 (Visibility Modifiers)

### 6.1 기본 원칙
```kotlin
// ✅ 올바름 - 필요한 것만 public
class AuthViewModel {
    // private 기본값 (가장 제한적)
    private val repository: AuthRepository

    // public 명시적으로 필요한 경우만
    val uiState: StateFlow<UiState>

    // internal이면 명시적으로 표시
    internal fun resetState() { }
}

// ❌ 잘못됨 - 모두 public
class AuthViewModel {
    var repository: AuthRepository  // public (기본값)
    var uiState: MutableStateFlow<UiState>  // public (변경 가능 위험!)
}
```

---

## 7. 주석 (Comments)

### 7.1 주석 규칙
```kotlin
// ✅ 올바름 - 왜(Why)를 설명
fun retryWithBackoff(maxRetries: Int = 3) {
    // 지수 백오프로 재시도하여 서버 부하 분산
    repeat(maxRetries) {
        val delayMs = 100 * (1 shl it)  // 100ms, 200ms, 400ms...
        delay(delayMs)
    }
}

// ✅ 올바름 - KDoc (public API)
/**
 * 사용자 로그인을 처리합니다.
 *
 * @param email 사용자 이메일
 * @param password 사용자 비밀번호
 * @return 성공 시 AuthUser, 실패 시 Result.Error
 * @throws NetworkException 네트워크 오류 시
 */
suspend fun login(email: String, password: String): Result<AuthUser>

// ❌ 잘못됨 - 무용지물한 주석
fun login(email: String, password: String) {
    // email을 가져온다
    // password를 가져온다
    // ...
}

// ❌ 잘못됨 - 코드를 반복
val user = repository.getUser()  // user 객체 가져오기
```

### 7.2 TODO / FIXME
```kotlin
// ✅ 올바름
fun loadData() {
    // TODO: 재시도 로직 추가 (issue #42)
    // FIXME: 메모리 누수 확인 필요
    fetchFromServer()
}
```

---

## 8. 람다와 고차 함수

### 8.1 람다 스타일
```kotlin
// ✅ 올바름 - 간단한 람다
list.map { it.name }

// ✅ 올바름 - 파라미터가 여러 개면 명시적
list.fold(0) { acc, item -> acc + item.price }

// ✅ 올바름 - 복잡하면 블록으로
list.forEach { item ->
    println("Processing ${item.name}")
    processItem(item)
}

// ❌ 잘못됨 - 한 글자 파라미터는 괜찮음 (관례)
list.map { item -> item.name }  // 'it' 사용 권장

// ❌ 잘못됨 - 복잡한 람다는 함수로 분리
list.forEach { item ->
    if (item.isActive) {
        process(item)
        updateUI(item)
        logAnalytics(item)
    }
}
// 대신 별도 함수로:
fun processActiveItem(item: Item) { ... }
list.forEach { processActiveItem(it) }
```

---

## 9. Collections

Immutable 선호 (`listOf()`, `mapOf()`), ArrayList 직접 사용 금지. 필요시 `mutableListOf()` 사용.

---

## 10. Scope Functions

apply(설정), also(로깅), let(null체크), run(식 실행) 사용. **과도한 중첩 금지**: `user?.let { ... }.let { ... }` 대신 `user?.profile?.avatar?.let { ... }` 사용.

---

## 11. 그 외 팁

### 11. Data Classes & Sealed Classes

Data class: 불변 모델에 사용, 자동으로 `equals/hashCode/toString/copy` 생성.
Sealed class: 타입 안전성, when 식에서 모든 케이스 강제.

### 12. Extension Functions

자주 사용되는 유틸리티를 extension으로 정의 (`fun Context.showToast()` 등).

