# Hilt Dependency Injection Guide

## Table of Contents

- [Overview](#overview)
- [Module Structure](#module-structure)
- [Module Definition Pattern](#module-definition-pattern)
- [Scopes](#scopes)
- [Constructor Injection](#constructor-injection)
- [Qualifiers](#qualifiers)
- [Bindings](#bindings-interface-to-implementation)
- [Entry Points](#entry-points)
- [Database Provision Pattern](#database-provision-pattern)
- [Context Injection](#context-injection)
- [Testing with Hilt](#testing-with-hilt)
- [Common Patterns](#common-patterns)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [Module Registration](#module-registration)

## Overview

This project uses **Hilt** for compile-time dependency injection. Hilt is a Dagger wrapper that simplifies DI setup for Android applications.

**Key Benefits**:
- Compile-time safety (errors caught at build time)
- Scope management (Singleton, Activity scope, etc.)
- Boilerplate reduction
- Auto-wiring through `@Inject`

## Module Structure

DI modules are organized **per feature**, located in `feature/[name]/di/`:

```
feature/drug/
├── di/
│   └── DrugModule.kt         # Drug-specific dependencies
├── data/
├── domain/
└── presentation/

feature/chat/
├── di/
│   └── ChatModule.kt         # Chat-specific dependencies
├── data/
├── domain/
└── presentation/

feature/auth/
├── di/
│   └── AuthModule.kt         # Auth-specific dependencies
├── data/
├── domain/
└── presentation/

app/di/
├── NetworkModule.kt          # Retrofit, OkHttp, API services (app-level)
├── AuthModule.kt             # Auth-related (if shared)
├── UserInfoModule.kt         # User info-related
└── LocationModule.kt         # Location-related
```

## Module Definition Pattern

### Feature-level Module

Each feature has its own module in `feature/[name]/di/`:

> **Note (real code)**: Currently, `feature/drug/di/DrugModule.kt` only provides `DrugDatabase` and `DrugPlanDao`. The `provideDrugRepository` and `provideSearchMedicinesUseCase` providers shown below represent the **target architecture pattern** to follow when adding repository or use case bindings.

```kotlin
// feature/drug/di/DrugModule.kt
package com.umc.hellodoctor.feature.drug.di

import android.content.Context
import androidx.room.Room
import com.umc.hellodoctor.feature.drug.data.database.DrugDatabase
import com.umc.hellodoctor.feature.drug.data.database.DrugPlanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DrugModule {

    @Provides
    @Singleton
    fun provideDrugDatabase(
        @ApplicationContext context: Context
    ): DrugDatabase = Room.databaseBuilder(
        context,
        DrugDatabase::class.java,
        "drug_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideDrugPlanDao(database: DrugDatabase): DrugPlanDao =
        database.drugPlanDao()

    @Provides
    @Singleton
    fun provideDrugRepository(
        medicineApi: MedicineApi,
        drugPlanDao: DrugPlanDao
    ): DrugRepository = DrugRepositoryImpl(medicineApi, drugPlanDao)

    @Provides
    @Singleton
    fun provideSearchMedicinesUseCase(
        drugRepository: DrugRepository
    ): SearchMedicinesUseCase = SearchMedicinesUseCase(drugRepository)
}
```

### App-level Modules

Global infrastructure goes in `app/di/`:

```kotlin
// app/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API Service Providers
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMedicineApi(retrofit: Retrofit): MedicineApi =
        retrofit.create(MedicineApi::class.java)

    @Provides
    @Singleton
    fun provideHospitalApi(retrofit: Retrofit): HospitalApi =
        retrofit.create(HospitalApi::class.java)

    @Provides
    @Singleton
    fun provideUserInfoApi(retrofit: Retrofit): UserInfoApi =
        retrofit.create(UserInfoApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitor(context)
}
```

## Scopes

### SingletonComponent (App-wide)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Provides
    @Singleton
    fun provideRepository(api: MyApi): MyRepository =
        MyRepository(api)
}
```

Used for:
- API clients (Retrofit, OkHttp)
- Repositories
- Use Cases
- Database providers
- Context-dependent utilities

### ActivityComponent (Activity scope)
```kotlin
@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @Provides
    fun provideActivityHelper(
        @ActivityContext context: Context
    ): ActivityHelper = ActivityHelper(context)
}
```

### FragmentComponent (Fragment scope)
```kotlin
@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {
    @Provides
    fun provideFragmentHelper(): FragmentHelper =
        FragmentHelper()
}
```

## Constructor Injection

### In ViewModels

```kotlin
@HiltViewModel
class DrugViewModel @Inject constructor(
    private val searchMedicinesUseCase: SearchMedicinesUseCase,
    private val getDrugPlansUseCase: GetDrugPlansUseCase
) : ViewModel() {
    // Implementation
}
```

### In Repository Implementations

```kotlin
class DrugRepositoryImpl @Inject constructor(
    private val medicineApi: MedicineApi,
    private val drugPlanDao: DrugPlanDao
) : DrugRepository {
    // Implementation
}
```

### In Use Cases

```kotlin
class SearchMedicinesUseCase @Inject constructor(
    private val drugRepository: DrugRepository
) {
    suspend operator fun invoke(keyword: String): List<Medicine> {
        // Implementation
    }
}
```

### In Interceptors

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Implementation
    }
}
```

## Qualifiers

Use when you need multiple implementations of the same interface:

### 1. Custom Qualifiers

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppPreferences
```

### 2. Using Qualifiers

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    @UserPreferences
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    @AppPreferences
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)
}

// Injection
class MyRepository @Inject constructor(
    @UserPreferences private val userPrefs: SharedPreferences,
    @AppPreferences private val appPrefs: SharedPreferences
)
```

## Bindings (Interface to Implementation)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object BindModule {

    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindDrugRepository(
        impl: DrugRepositoryImpl
    ): DrugRepository
}
```

Note: Use `abstract` functions with `@Binds` instead of `@Provides` for simpler interface-to-implementation bindings.

## Entry Points

### Application Class

```kotlin
@HiltAndroidApp
class HelloDoctorApp : Application() {
    // Hilt initializes here
}
```

### Activities & Fragments

```kotlin
class MainActivity : AppCompatActivity() {
    // No annotation needed - Hilt finds dependencies automatically
}

class MyFragment : Fragment() {
    private val viewModel: MyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // viewModel is injected
    }
}
```

## Database Provision Pattern

The `drug` feature uses this pattern. → See [Module Definition Pattern](#module-definition-pattern) for the full `DrugModule` example.

```kotlin
// feature/chat/di/ChatModule.kt
@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideChatDatabase(
        @ApplicationContext context: Context
    ): ChatDatabase = Room.databaseBuilder(
        context,
        ChatDatabase::class.java,
        "chat_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideChatSessionDao(
        database: ChatDatabase
    ): ChatSessionDao = database.chatSessionDao()
}
```

## Context Injection

```kotlin
class MyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getAppName(): String = context.getString(R.string.app_name)
}
```

## Feature Module Pattern

```kotlin
// feature/auth/di/AuthModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi
    ): AuthRepository = AuthRepositoryImpl(authApi)

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)
}
```

The `drug` feature's module is in [Module Definition Pattern](#module-definition-pattern).

## Testing with Hilt

For unit tests, use Hilt's test modules:

```kotlin
// For integration tests
@HiltAndroidTest
class MyIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testWithHilt() {
        // Test with Hilt-injected dependencies
    }
}

// For unit tests with mocked dependencies
class MyRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val api: MyApi = mockk()
    private val repository = MyRepository(api)

    @Test
    fun testSearch() {
        // Test without Hilt (simpler for unit tests)
    }
}
```

## Common Patterns

### Lazy Initialization

```kotlin
// feature/drug/di/DrugModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DrugModule {

    @Provides
    @Singleton
    fun provideLazyRepository(
        medicineApi: MedicineApi
    ): Lazy<DrugRepository> = lazy {
        DrugRepositoryImpl(medicineApi, mockk())
    }
}

// Usage
class MyViewModel @Inject constructor(
    private val lazyRepository: Lazy<DrugRepository>
) {
    fun loadData() {
        val repo = lazyRepository.value  // Initialized on first access
    }
}
```

### Provider (Factory Pattern)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyModule {

    @Provides
    @Singleton
    fun provideRepositoryProvider(
        api: MyApi
    ): Provider<MyRepository> = Provider { MyRepository(api) }
}

// Usage - creates new instance each time
class MyService @Inject constructor(
    private val repositoryProvider: Provider<MyRepository>
) {
    fun useNewRepository() {
        val newRepo = repositoryProvider.get()
    }
}
```

## Best Practices

### 1. Organize Modules by Feature
```kotlin
// Good: modules stay with their features
feature/drug/di/DrugModule.kt
feature/auth/di/AuthModule.kt
feature/chat/di/ChatModule.kt

// App-level shared modules
app/di/NetworkModule.kt
app/di/LocationModule.kt
```

### 2. Use Binds for Interfaces
```kotlin
// Good: explicit binding
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: RepositoryImpl): Repository
}
```

### 3. Scope Appropriately
```kotlin
// Singleton for expensive resources
@Provides
@Singleton
fun provideRetrofit(client: OkHttpClient): Retrofit = ...

// Activity scope for activity-specific
@Provides
fun provideActivityHelper(@ActivityContext context: Context): Helper = ...
```

### 4. Use Qualifiers Sparingly
```kotlin
// Good: only when necessary
@Provides
@Singleton
@UserPreferences
fun provideUserPrefs(...): SharedPreferences = ...
```

### 5. Document Complex Bindings
```kotlin
/**
 * Provides the main API client for HelloDoctor backend.
 * Includes authentication interceptor and 30-second timeouts.
 */
@Provides
@Singleton
fun provideRetrofit(client: OkHttpClient): Retrofit = ...
```

## Troubleshooting

### Issue: "No binding provided"
```kotlin
// Error: No binding for MyClass
// Solution: Add @Inject constructor or provide in module
@Inject constructor(...)  // Auto-inject if has @Inject constructor
// or
@Provides fun provideMyClass(): MyClass = ...
```

### Issue: "Circular dependency"
```kotlin
// Error: Circular dependency
// Solution: Break the cycle with Lazy or Provider
class A @Inject constructor(lazyB: Lazy<B>)
class B @Inject constructor(a: A)
```

### Issue: "Scope mismatch"
```kotlin
// Error: Scope mismatch
// Solution: Ensure scopes match
@Singleton  // Module
fun provideRepository(...): Repository = ...

// Can be injected into Singleton, but not Activity scope
```

## Module Registration

When you create a new feature module in `feature/[name]/di/`, Hilt automatically discovers it via `@InstallIn(SingletonComponent::class)`. No explicit registration needed.

However, ensure:
1. Module has `@Module` annotation
2. Module has `@InstallIn(SingletonComponent::class)` or appropriate component
3. Providers/Bindings have `@Provides/@Binds` annotations
4. Build with `./gradlew build` to trigger Hilt code generation

## Related Documentation

- [Architecture](architecture.md) - Dependency direction and layer responsibilities
- [Coding Conventions](coding-conventions.md) - Hilt-specific code style
- [API Integration](api-integration.md) - Network module configuration
- [Testing](testing.md) - Hilt testing patterns

---

**Last Updated**: 2026-03-31
