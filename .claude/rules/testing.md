# 테스트 규칙

## 개요

HelloDoctor-android는 **단위 테스트 (Unit Test)**를 중심으로 합니다.

- **주요 테스트**: ViewModel, UseCase, Repository
- **프레임워크**: JUnit4, Mockito, Kotest (선택)
- **권장 커버리지**: 60% 이상 (핵심 로직)

---

## 1. 테스트 구조

### 1.1 테스트 파일 위치

```
main code:  feature/auth/presentation/LoginViewModel.kt
test code:  feature/auth/presentation/LoginViewModelTest.kt

main code:  feature/auth/domain/usecase/LoginUseCase.kt
test code:  feature/auth/domain/usecase/LoginUseCaseTest.kt

main code:  feature/auth/data/repository/AuthRepositoryImpl.kt
test code:  feature/auth/data/repository/AuthRepositoryImplTest.kt
```

### 1.2 테스트 파일 이름

`{클래스명}Test` 사용. `Tests`, `Test_`, `UnitTest` 등 접미사 금지.

---

## 2. 테스트 작성 패턴 (AAA Pattern)

```kotlin
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel
    private val loginUseCase: LoginUseCase = mock()

    @Before
    fun setUp() {
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun onLoginClicked_whenSuccess_thenStateIsSuccess() {
        // Arrange (준비)
        val email = "user@example.com"
        val password = "password"
        whenever(loginUseCase(email, password))
            .thenReturn(Result.Success(AuthUser(1, email, "John", UserRole.PATIENT)))

        // Act (실행)
        viewModel.onLoginClicked(email, password)

        // Assert (검증)
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(LoginUiState.Success::class.java)
    }
}
```

**규칙**:
- `@Before setUp()`: mock 생성 및 초기화
- `@get:Rule`: MainDispatcherRule (Coroutine), InstantTaskExecutorRule (StateFlow)
- `runTest { }`: coroutine 자동 완료 대기
- `verify()`: 메서드 호출 확인

---

## 3. ViewModel 테스트

Happy path (성공), Error path (실패), Edge case (경계값) 테스트 작성.

```kotlin
@Test
fun onLoginClicked_whenError_thenStateIsError() = runTest {
    whenever(loginUseCase(email, password))
        .thenReturn(Result.Error("Invalid credentials"))
    viewModel.onLoginClicked(email, password)
    assertThat(viewModel.uiState.value).isInstanceOf(LoginUiState.Error::class.java)
}
```

---

## 4. UseCase 테스트

```kotlin
class LoginUseCaseTest {
    private val authRepository: AuthRepository = mock()
    private val loginUseCase = LoginUseCase(authRepository)

    @Test
    fun invoke_whenValidInput_thenReturnsSuccess() = runTest {
        whenever(authRepository.login(email, password))
            .thenReturn(Result.Success(user))
        val result = loginUseCase(email, password)
        assertThat(result).isInstanceOf(Result.Success::class.java)
    }

    @Test
    fun invoke_whenRepositoryFails_thenReturnsError() = runTest {
        whenever(authRepository.login(email, password))
            .thenReturn(Result.Error("Network error"))
        val result = loginUseCase(email, password)
        assertThat(result).isInstanceOf(Result.Error::class.java)
    }
}
```

**규칙**: 입력 검증, 성공, 실패 케이스별로 분리. `verify()` 사용해 호출 순서 확인.

---

## 5. Repository 테스트

```kotlin
class AuthRepositoryImplTest {
    private val remoteDataSource: AuthRemoteDataSource = mock()
    private val localDataSource: AuthLocalDataSource = mock()
    private val repository = AuthRepositoryImpl(remoteDataSource, localDataSource)

    @Test
    fun login_savesUserToLocal() = runTest {
        whenever(remoteDataSource.login(email, password))
            .thenReturn(LoginResponse(1, email, "John", "PATIENT", "token"))
        val result = repository.login(email, password)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(localDataSource).saveUser(any())
    }
}
```

---

## 6. Mock vs Spy

- **Mock**: 전체 가짜 객체, `whenever().thenReturn()` 사용
- **Spy**: 실제 객체, 특정 메서드만 오버라이드, 실제 로직이 있는 객체(Mapper)에 사용

```kotlin
val mockRepository = mock<AuthRepository>()
val spyMapper = spy(AuthMapper())
verify(mockRepository).login(any(), any())  // 호출 확인
```

---

## 7. 테스트 함수명 규칙

Given-When-Then 패턴: `{메서드}_when{조건}_then{결과}`

```kotlin
fun onLoginClicked_whenInputIsValid_thenStateIsLoading()
fun login_whenNetworkError_thenReturnsError()
fun invoke_whenInvalidEmail_thenReturnsError()
```

---

## 8. 테스트 체크리스트

- [ ] Happy path (성공 케이스)
- [ ] Error path (실패 케이스)
- [ ] Edge case (경계값)
- [ ] Mock/Spy 설정 정확
- [ ] verify() 호출 확인
- [ ] 테스트 이름 명확
- [ ] 전체 테스트 통과

---

## 참고

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core)
- [Coroutine Testing](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html#testing)
