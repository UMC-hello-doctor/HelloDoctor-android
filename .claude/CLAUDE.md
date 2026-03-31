# Claude Code Project Context — HelloDoctor-android

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Package Responsibilities](#package-responsibilities)
- [Detailed Rules](#detailed-rules)
- [Build Configuration](#build-configuration)
- [Test Structure](#test-structure)
- [Commit Convention](#commit-convention)
- [Code Style Checking](#code-style-checking)
- [Key Project Files](#key-project-files)
- [Next Steps](#next-steps)

## Project Overview

**HelloDoctor-android** is an Android-based medical consultation application.

- **Package**: `com.umc.hellodoctor`
- **Target API**: API 36 (Android 15)
- **Min API**: API 24 (Android 7.0)
- **Language**: Kotlin
- **Build System**: Gradle 8.x + KSP

## Architecture

This project follows **Clean Architecture** with a **Feature-based Modular** structure.

### Top-level Package Structure

```
com.umc.hellodoctor/
├── app/              # App entry point, DI, navigation
├── core/             # Common utilities, network, UI components
└── feature/          # Business logic modules
    ├── auth/         # Authentication/Login
    ├── chat/         # AI consultation
    ├── drug/         # Medicine information search
    ├── navermap/     # Map (hospital locations)
    ├── userinfo/     # User information
    ├── language/     # Multi-language support
    ├── menu/         # Menu
    └── basicFolder/  # (Template)
```

### Layer Structure for Each Feature

All features follow the 3-layer Clean Architecture pattern:

```
feature/[featureName]/
├── data/
│   ├── api/                    # Retrofit API interfaces
│   ├── repository/             # Repository implementations (data layer)
│   ├── model/                  # API request/response DTOs
│   ├── database/               # Room database entities, DAOs
│   └── service/                # Services (e.g., AlarmService)
│
├── domain/
│   ├── repository/             # Repository interfaces (abstraction)
│   ├── model/                  # Domain models (UI-agnostic pure models)
│   └── usecase/                # Use cases (business logic)
│
└── presentation/
    ├── ui/                     # Fragments, Activities
    ├── viewmodel/              # ViewModels
    └── adapter/                # RecyclerView Adapters
```

## Technology Stack

### Core Dependencies
- **Kotlin**: 1.9.x
- **AndroidX**: Core libraries (lifecycle, navigation, room, datastore)
- **Coroutines**: Async operations
- **LiveData/StateFlow**: UI state management
- **ViewBinding**: Type-safe UI binding

### Network & API
- **Retrofit 2**: HTTP client
- **OkHttp 4**: HTTP interceptors, logging
- **Gson**: JSON serialization
- **AuthInterceptor**: Automatic JWT token injection

### Dependency Injection
- **Hilt**: Compile-time dependency injection
- Module location: `app/di/`
- Scope: Primarily `SingletonComponent::class`

### Data & Storage
- **Room**: Local database
- **DataStore**: Lightweight preferences
- **SharedPreferences**: Legacy settings storage (token management)

### Code Quality
- **ktlint**: Kotlin code style checking
- **detekt**: Static code analysis
- **JaCoCo**: Test coverage measurement

### UI Libraries
- **Navigation Component**: Screen navigation
- **Material Design 3**: UI components
- **CameraX**: Camera functionality
- **Naver Map SDK**: Map display

## Package Responsibilities

### `app/`
- **HelloDoctorApp**: Application class, Hilt initialization
- **MainActivity**: Navigation host, root container
- **di/**: All Hilt modules
  - `NetworkModule`: Retrofit, OkHttp client, all API services
  - `AuthModule`: Authentication-related dependencies
  - `UserInfoModule`: User info-related dependencies
  - `LocationModule`: Location-related dependencies

### `core/`
- **network/**: BaseResponse, AuthInterceptor, common network classes
- **ui/**: Common UI components, theme, extensions
- **util/**: Common utility functions
- **location/**: Location permissions, GPS providers
- **permission/**: Permission management
- **notification/**: Notification helpers

### `feature/[name]/`
- **data/**: All network/database access logic
- **domain/**: Business logic, Repository interfaces
- **presentation/**: UI, ViewModels, Fragments/Activities

## Detailed Rules

For detailed rules, refer to the files in `.claude/rules/`:

- [`architecture.md`](rules/architecture.md) — Clean Architecture design and patterns
- [`coding-conventions.md`](rules/coding-conventions.md) — Kotlin coding conventions
- [`commit-conventions.md`](rules/commit-conventions.md) — Git commit and branch conventions
- [`hilt-di.md`](rules/hilt-di.md) — Hilt dependency injection guide
- [`api-integration.md`](rules/api-integration.md) — Retrofit and network layer guide
- [`testing.md`](rules/testing.md) — Test writing rules
- [`ui-patterns.md`](rules/ui-patterns.md) — UI layer patterns and ViewModel guide

## Build Configuration

### BuildConfig Fields
The project reads the following fields from `gradle.properties`:

```properties
SERVER_BASE_URL=https://api.hellodoctor.dev
AI_API_KEY=<your-key>
NAVER_MAP_CLIENT_ID=<your-id>
NAVER_MAP_CLIENT_SECRET=<your-secret>
GOOGLE_OAUTH_CLIENT_ID=<your-id>
```

### Network Timeouts
All HTTP requests: **30 seconds** (connection, read, write, call timeouts are the same)

## Test Structure

```
app/src/
├── test/              # Unit tests
│   └── java/.../      # JUnit tests
└── androidTest/       # Instrumentation tests
    └── java/.../      # AndroidJUnit tests
```

## Commit Convention

This project uses **Conventional Commits** format in **Korean**:

```
<type>(<scope>): <subject>

<body>
```

**Types**: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `perf`
**Scope**: Feature name (e.g., `auth`, `drug`, `network`)
**Subject**: Brief description (max 50 chars)

→ For comprehensive commit examples: [commit-conventions.md — Examples](rules/commit-conventions.md#examples)

## Code Style Checking

```bash
./gradlew ktlintCheck  # Kotlin format checking
./gradlew detekt       # Static analysis
./gradlew compileDebugKotlin  # Compilation checking
```

## Key Project Files

- `build.gradle.kts` — Root build configuration
- `app/build.gradle.kts` — App module configuration (Hilt, KSP, plugins)
- `settings.gradle.kts` — Module composition
- `gradle.properties` — Global configuration (excluded from git)
- `.github/` — GitHub Actions, issue/PR templates, label config
- `.claude/` — Claude Code project context (this directory)

## Next Steps

When adding a new feature:

1. Read `.claude/rules/` documentation to understand architecture patterns
2. Refer to existing features (e.g., `auth`, `drug`) and copy structure for new feature package
3. Implement in order: data → domain → presentation
4. Register Hilt module in `app/di/`
5. Run code style checks (`ktlintCheck`) and tests before committing

---

**Last Updated**: 2026-03-31
**Maintained By**: UMC Hello Doctor Dev Team
