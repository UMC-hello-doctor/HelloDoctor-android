# 아키텍처 규칙

## 개요

HelloDoctor-android는 **클린 아키텍처** 원칙을 따릅니다.
각 기능은 3계층(Presentation, Domain, Data)으로 나뉘며, 계층 간 의존성은 단방향입니다.

## 계층 구조

```
Presentation (UI/ViewModel)
     ↓ (UseCase/Repository 호출)
Domain (UseCase/Repository Interface)
     ↓ (Repository 구현체 호출)
Data (API/DB)
```

### ❌ 피해야 할 의존성

- **UI가 Data에 직접 접근** ❌
- **Presentation이 다른 feature의 domain에 의존** ❌
- **Domain이 Android framework에 의존** ❌
- **Feature 간 직접 통신** ❌

---

## 1. Presentation Layer (UI + ViewModel)

### 위치
```
feature/{기능명}/presentation/
```

### 책임
- ViewModel 구현 (비즈니스 로직 처리, 상태 관리)
- UI State 정의 (화면 상태 모델)
- UI Event 정의 (사용자 입력 처리)

### 구현 규칙

#### 1.1 ViewModel
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginClicked(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(email, password)
            _uiState.value = when (result) {
                is Result.Success -> LoginUiState.Success(result.data)
                is Result.Error -> LoginUiState.Error(result.message)
            }
        }
    }
}
```

**규칙**:
- `@HiltViewModel` 어노테이션 필수
- StateFlow/LiveData로 상태 노출
- Coroutine 사용 (viewModelScope)
- Repository 인터페이스에만 의존 (구현체 X)

#### 1.2 UI State & Event
```kotlin
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: AuthUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

sealed class LoginEvent {
    data class OnEmailChanged(val email: String) : LoginEvent()
    object OnLoginClicked : LoginEvent()
}
```

**규칙**: sealed class로 타입 안전성 확보, 화면 상태와 이벤트는 불변 객체로 정의

---

## 2. Domain Layer (UseCase + Repository Interface)

### 위치
```
feature/{기능명}/domain/
```

### 구조
```
domain/
├─ model/        # 도메인 엔티티 (AuthUser, Doctor 등)
├─ repository/   # Repository 인터페이스
└─ usecase/      # UseCase 클래스
```

### 2.1 Domain Model
```kotlin
data class AuthUser(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole
)

enum class UserRole {
    PATIENT, DOCTOR
}
```

**규칙**:
- Android framework 의존성 없음
- 불변 데이터 클래스
- 서버 응답과 분리된 비즈니스 모델

### 2.2 Repository Interface
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<AuthUser>
}
```

**규칙**:
- 인터페이스만 정의 (구현 X)
- suspend 함수 (Coroutine 지원)
- Result 타입 사용 (Success/Error 추상화)

### 2.3 UseCase
```kotlin
@Singleton
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateEmailUseCase: ValidateEmailUseCase
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<AuthUser> {
        // 입력 검증
        validateEmailUseCase(email).onFailure { return it }

        // Repository 호출
        return authRepository.login(email, password)
    }
}
```

**규칙**:
- `invoke` 연산자 오버로드 (선택사항)
- 단일 책임 (하나의 비즈니스 흐름)
- Android 의존성 없음
- Suspend 함수 (비동기)

---

## 3. Data Layer (RepositoryImpl + DataSource)

### 위치
```
feature/{기능명}/data/
```

### 구조
```
data/
├─ repository/   # Repository 구현체 (RepositoryImpl)
├─ remote/       # API 통신 (ApiInterface, RemoteDataSource)
├─ local/        # 로컬 저장 (DAO, LocalDataSource, Entity)
└─ mapper/       # DTO ↔ Domain Model 변환
```

### 3.1 RemoteDataSource (API)
```kotlin
interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): LoginResponse
}

class AuthRemoteDataSourceImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): LoginResponse {
        return authApi.login(LoginRequest(email, password))
    }
}

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

data class LoginResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
    val accessToken: String
)
```

**규칙**:
- Remote/Local DataSource는 인터페이스로 분리
- DTO 사용 (Domain Model과 분리)
- Suspend 함수 (비동기)

### 3.2 LocalDataSource (DB)
```kotlin
interface AuthLocalDataSource {
    suspend fun saveUser(userEntity: UserEntity)
    suspend fun getUser(): UserEntity?
    suspend fun deleteUser()
}

class AuthLocalDataSourceImpl @Inject constructor(
    private val userDao: UserDao
) : AuthLocalDataSource {
    override suspend fun saveUser(userEntity: UserEntity) = userDao.insert(userEntity)
    override suspend fun getUser(): UserEntity? = userDao.getUser()
    override suspend fun deleteUser() = userDao.delete()
}
```

### 3.3 RepositoryImpl
```kotlin
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val authMapper: AuthMapper
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val response = remoteDataSource.login(email, password)
            localDataSource.saveUser(authMapper.toEntity(response))
            Result.Success(authMapper.toDomain(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
```

**규칙**: Remote/Local 호출, 매핑, 저장을 순차적으로 처리하고 예외를 Result로 변환

### 3.4 Mapper
```kotlin
class AuthMapper {
    fun toDomain(response: LoginResponse): AuthUser = AuthUser(
        id = response.id,
        email = response.email,
        name = response.name,
        role = UserRole.valueOf(response.role)
    )
}
```

**규칙**: DTO/Entity → Domain Model 변환, 필요한 1개 방향 매핑만 유지

---

## 4. Core 모듈 (공용 코드)

### 사용 기준

공통으로 사용되는 코드는 **core** 모듈로 올립니다.

#### ✅ Core로 올릴 대상
- 2개 이상 feature에서 사용되는 컴포넌트
- UI 컴포넌트 (Button, TextField, Dialog)
- 네트워크 공용부 (ApiClient, AuthInterceptor)
- 유틸리티 (DateTimeUtil, Logger, ResourceProvider)
- 공용 모델 (ApiError, Result)

#### ❌ Core로 올리지 말 대상
- 특정 feature의 도메인 모델
- 특정 기능의 UseCase
- 특정 화면의 UI State/Event

### Core 구조
```
core/
├─ design/       # 색상, 폰트, 마진 등 디자인 시스템
├─ model/        # 공용 모델 (ApiError, Result 등)
├─ network/      # ApiClient, AuthInterceptor, NetworkResult
├─ ui/           # 공용 컴포넌트, 확장함수, 테마
└─ util/         # 유틸리티 함수
```

---

## 5. DI (의존성 주입)

### 규칙

모든 DI는 **app/di/** 에서 관리합니다.

#### 5.1 Module 분리
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Singleton
    @Provides
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl
}
```

**규칙**: Interface → Impl 바인딩, @Singleton/scope 명시, 라이프사이클에 맞는 Scope 선택

---

## 6. 계층 간 통신

- **Data ↔ Domain**: RepositoryImpl이 Repository 인터페이스 구현
- **Domain → Presentation**: UseCase를 통해 통신, Result로 결과 전달
- **UI ↔ ViewModel**: StateFlow/LiveData와 이벤트로 상태 업데이트

---

## 요약

| 계층 | 책임 | 의존성 | 핵심 |
|-----|------|--------|------|
| **Presentation** | UI 렌더링, 상태 관리 | Domain (UseCase) | ViewModel, StateFlow |
| **Domain** | 비즈니스 로직, 규칙 | None | UseCase, Repository Interface |
| **Data** | 실제 구현 (API, DB) | Domain (Interface) | RepositoryImpl, DataSource |

