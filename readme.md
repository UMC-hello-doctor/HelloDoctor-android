# hellodoctor 패키지 구조 가이드

## 전체 개요

umc.hellodoctor
├─ app
├─ core
└─ feature

 

- `app`  : 앱 엔트리, DI, 네비게이션 등 전체 조립부.
- `core` : 여러 기능에서 공통으로 사용하는 UI/네트워크/유틸 모음.
- `feature` : 실제 비즈니스 기능(로그인, 홈 등)을 모아두는 영역.
- 현재 `feature` 아래에는 템플릿용 `basicFolder`가 있으며, 이후 `auth`, `home` 등 실제 기능으로 복사해 사용한다.

---

## 1. app 패키지

umc.hellodoctor.app
├─ HelloDoctorApp
├─ MainActivity
└─ di
├─ AppModule
├─ NetworkModule
└─ AuthModule

 

### HelloDoctorApp
- `Application` 클래스.
- DI(Hilt) 초기화, 전역 설정, 로그/디버그 설정 등 **앱 레벨 부트스트랩** 담당.

### MainActivity
- 앱의 진입 Activity.
- `NavHostFragment`를 붙이고, 네비게이션 그래프(`nav_graph.xml`)를 사용하는 루트 화면.
- 공통 툴바/BottomNav 등 앱 전역 UI 컨테이너 역할.

### app/di 패키지
DI 모듈 전용 패키지. Hilt(Koin 등)에서 사용할 의존성 정의를 모아둔다.

- **AppModule**
    - 앱 전반에서 쓰는 공통 의존성 제공.
    - 예: `Context`, `SharedPreferences`, `ResourceProvider`, `Logger` 등.

- **NetworkModule**
    - 네트워크 관련 의존성 제공.
    - 예: `OkHttpClient`, `Retrofit`, `Gson`, `ApiClient`, `AuthInterceptor` 바인딩 등.

- **AuthModule**
    - 인증/로그인 관련 의존성 제공.
    - 예: `AuthRepository` ↔ `AuthRepositoryImpl` 바인딩, 각종 UseCase(`LoginUseCase`, `GoogleLoginUseCase` 등) 제공.

> 추후 네비게이션 헬퍼가 늘어나면  
> `umc.hellodoctor.app.navigation` 패키지를 만들어 `NavGraph.kt`, `Destinations.kt` 등을 분리해서 관리한다.

---

## 2. core 패키지

umc.hellodoctor.core
├─ design
├─ model
├─ network
├─ ui
│ ├─ component
│ ├─ extension
│ └─ theme
└─ util

 

### core/design
- 디자인 시스템 관련 상수/헬퍼.
- 예: 공통 마진/패딩(`Dimens`), 라운드 값, 쉐이프 정의 등.

### core/model
- 여러 feature에서 재사용할 수 있는 **공통 모델**.
- 예: 공통 에러 모델(`ApiError`), 에러 타입(`ErrorType`), 공용 `Result` 타입 등이 위치.

### core/network
네트워크 관련 공용 구성 요소.

- **ApiClient**
    - Retrofit, OkHttpClient, Gson 등 네트워크 클라이언트를 생성하는 팩토리/프로바이더.
    - baseUrl, 타임아웃, 로깅 설정 등 공통 네트워크 설정을 중앙에서 관리.

- **AuthInterceptor**
    - 서버 JWT(액세스 토큰)를 `Authorization: Bearer <token>` 헤더에 붙이는 OkHttp Interceptor.
    - 자동 토큰 주입 및 401 처리 등의 공통 로직을 구현.

- **NetworkResult**
    - 네트워크 호출 결과를 래핑하는 sealed class.
    - 예: `Success<T>`, `Error`, `Loading` 등으로 표현해 ViewModel에서 일관되게 처리.

### core/ui

#### ui/component
- 여러 feature에서 공통으로 재사용할 **UI 컴포넌트**.
- 예: 커스텀 버튼, 공통 TextField, 로딩 뷰, 공통 Dialog 등.

#### ui/extension
- View, Fragment, Activity 등에 대한 **확장 함수** 모음.
- 예: `View.visible()`, `View.gone()`, `Fragment.showToast()`, `EditText.afterTextChanged {}` 등.

#### ui/theme
- 색상, 폰트, 스타일 등 **테마 관련 헬퍼 클래스**.
- XML 스타일/테마를 사용할 때 Kotlin 코드에서 재사용하거나,
  다크 모드/테마 전환 로직을 보조하기 위한 유틸.

### core/util
- 순수 유틸리티 / 헬퍼 코드.

예시:
- `DateTimeUtil` : 날짜/시간 포맷, 파싱 유틸.
- `ResourceProvider` : Context 없이 문자열/리소스 접근을 위한 추상화 (테스트 용이성 향상).
- `Logger` : 공통 로깅 유틸 (Debug/Release에 따라 로그 출력 제어).

---

## 3. feature 패키지

현재 구조:

umc.hellodoctor.feature
└─ basicFolder
├─ data
├─ domain
├─ presentation
└─ ui

 

- `basicFolder`는 **기능 템플릿** 역할을 한다.
- 실제 기능을 만들 때는 `basicFolder`를 복사해서  
  `auth`, `home`, `settings` 등 기능 이름으로 패키지를 만든 후 동일한 레이어 구조를 사용한다.

향후 목표 구조 예시:

feature
├─ auth
│ ├─ data
│ ├─ domain
│ ├─ presentation
│ └─ ui
├─ home
│ ├─ data
│ ├─ domain
│ ├─ presentation
│ └─ ui
└─ settings
├─ data
├─ domain
├─ presentation
└─ ui

 

### feature/basicFolder/ui
- **화면 계층**: Activity/Fragment + XML 레이아웃과 1:1로 매칭되는 UI 레이어.
- 예: `LoginFragment`, `HomeFragment`, `ProfileFragment` 등.
- 역할:
    - 사용자 입력 처리(버튼 클릭, 스크롤 등).
    - `presentation` 레이어의 ViewModel을 구독하여 UI 상태를 반영.
    - 에러/로딩/네비게이션 이벤트를 ViewModel로부터 받아 화면 전환 처리.

### feature/basicFolder/presentation
- ViewModel, UI 상태, 이벤트를 담당하는 계층.
- 예:
    - `LoginViewModel`, `LoginUiState`, `HomeViewModel` 등.
- 특징:
    - UI 레이어에서 발생한 이벤트를 받아 **UseCase(domain)**를 호출.
    - LiveData/StateFlow 등으로 UI 상태를 노출.
    - data 레이어에 직접 의존하지 않고, **domain 레이어의 Repository 인터페이스**에만 의존.

### feature/basicFolder/domain
- 비즈니스 규칙과 핵심 로직이 위치하는 계층.

예시 구조:

feature/basicFolder/domain
├─ model // 도메인 엔티티
├─ repository // Repository 인터페이스
└─ usecase // 유스케이스

 

- `model` : 기능별 도메인 모델 (예: `AuthUser`, `Doctor`, `Appointment` 등).
- `repository` : UseCase가 의존하는 Repository 인터페이스 정의 (예: `AuthRepository`).
- `usecase` : 구체적인 작업 단위(로그인, 로그아웃, 데이터 로딩 등)를 캡슐화한 클래스.

### feature/basicFolder/data
- 실제 구현부 (네트워크, DB, 캐시 등)를 담당하는 계층.

예시 구조:

feature/basicFolder/data
├─ repository // Repository 구현체
├─ remote // Retrofit API, 원격 데이터 소스
├─ local // Room, DataStore, SharedPreferences
└─ mapper // DTO ↔ 도메인 모델 변환

 

- `repository` : domain의 Repository 인터페이스를 구현 (`AuthRepositoryImpl` 등).
- `remote` : Retrofit 인터페이스(`AuthApi`)와 원격 데이터 소스(`AuthRemoteDataSource`).
- `local` : 로컬 저장소(DataStore, Room, SharedPreferences 등)를 다루는 데이터 소스(`AuthLocalDataSource` 등).
- `mapper` : 서버/DB DTO ↔ domain 모델 간 변환 책임을 담당.

---

## 4. 새 기능을 추가할 때 사용 패턴

1. `feature/basicFolder`를 복사해서 `feature/auth`처럼 **기능 이름**으로 변경한다.
2. 각 레이어에 기능에 맞는 클래스 추가:
    - `ui`        : Fragment + XML 레이아웃
    - `presentation` : ViewModel + UiState
    - `domain`    : UseCase + Repository 인터페이스 + 도메인 모델
    - `data`      : RepositoryImpl + Remote/Local DataSource + Mapper
3. 공통 코드가 필요하면 feature에 두지 말고 **core**로 올려서 재사용성을 높인다.
4. DI는 `app.di`에서 조립:
    - `NetworkModule`에서 Retrofit/OkHttp 제공.
    - `AuthModule` 등에서 `AuthRepositoryImpl`을 `AuthRepository`에 바인딩하고, UseCase를 제공.

이 문서를 기준으로, 새로운 기능이나 공통 모듈을 추가할 때 구조와 위치를 통일해서 관리한다.