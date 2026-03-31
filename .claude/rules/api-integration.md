# API Integration & Network Layer Guide

## Table of Contents

- [Overview](#overview)
- [Network Architecture](#network-architecture)
- [Defining APIs](#defining-apis)
- [Repository Implementation](#repository-implementation)
- [Domain Layer Repository Interface](#domain-layer-repository-interface)
- [AuthInterceptor](#authinterceptor)
- [Retrofit Configuration](#retrofit-configuration)
- [Using APIs in ViewModels](#using-apis-in-viewmodels)
- [Error Handling](#error-handling)
- [Adding New API Endpoints](#adding-new-api-endpoints)
- [Request & Response Examples](#request--response-examples)
- [Testing Network Layer](#testing-network-layer)
- [Best Practices](#best-practices)

## Overview

Network operations are handled by **Retrofit 2** with **OkHttp 4**, configured in `app/di/NetworkModule.kt` and used throughout the application following Clean Architecture principles.

For architecture details, see [architecture.md](architecture.md).

## Network Architecture

### Layer Organization

```
core/network/
├── BaseResponse.kt          # Wrapper for all API responses
├── AuthInterceptor.kt       # Auto-injects JWT tokens
└── status/
    ├── NetworkMonitor.kt    # Monitors network connectivity
    ├── NetworkStatus.kt     # Status enum
    └── NetworkViewModel.kt  # Network state management

feature/[name]/data/
├── api/
│   └── MyApi.kt             # Retrofit interface
├── model/
│   ├── MyRequest.kt         # Request DTOs
│   └── MyResponse.kt        # Response DTOs
└── repository/
    └── MyRepositoryImpl.kt   # Implementation (handles API calls)
```

### BaseResponse Wrapper

All API responses are wrapped in `BaseResponse`:

```kotlin
// core/network/BaseResponse.kt
data class BaseResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val result: T  // Generic for flexible types
)
```

Every API endpoint returns this structure:
```json
{
  "success": true,
  "code": "000",
  "message": "Success",
  "result": { /* actual data */ }
}
```

## Defining APIs

### 1. Create API Interface

```kotlin
// feature/drug/data/api/MedicineApi.kt
package com.umc.hellodoctor.feature.drug.data.api

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import retrofit2.http.GET
import retrofit2.http.Query

interface MedicineApi {
    @GET("v1/medicines/search")
    suspend fun searchMedicines(
        @Query("name") keyword: String
    ): BaseResponse<List<MedicineSearchItem>>

    @GET("v1/medicines/{id}")
    suspend fun getMedicine(
        @Path("id") medicineId: String
    ): BaseResponse<MedicineSearchItem>
}
```

**Rules**:
- Always use `suspend` for async operations
- Return `BaseResponse<T>` (not just `T`)
- Use meaningful HTTP verbs (`@GET`, `@POST`, etc.)
- Document parameters with `@Query`, `@Path`, `@Body`
- Put API interface in `data/api/` package

### 2. Create DTOs (Request/Response Models)

```kotlin
// feature/drug/data/model/MedicineSearchItem.kt
package com.umc.hellodoctor.feature.drug.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicineSearchItem(
    @SerializedName("medicineId")
    val medicineId: String,
    @SerializedName("medicineName")
    val medicineName: String,
    @SerializedName("entpName")
    val entpName: String,
    @SerializedName("medicineImage")
    val medicineImage: String,
    @SerializedName("efficacy")
    val efficacy: String
) : Parcelable
```

**Rules**:
- Use `@SerializedName` for JSON field mapping to handle API response field names
- Implement `@Parcelize Parcelable` for Fragment navigation with safe args
- Make fields nullable with `? = null` for optional fields
- Use data classes for immutability
- Put models in `data/model/` package
- Never expose DTOs in presentation layer

For coding conventions details, see [coding-conventions.md](coding-conventions.md).

## Repository Implementation

> **Note**: `MedicineRepositoryImpl` does not yet exist in production code. `DrugViewModel` currently injects `MedicineApi` directly. This pattern below represents the **intended target architecture** when the repository layer is added.

### Data Layer Repository

```kotlin
// feature/drug/data/repository/MedicineRepositoryImpl.kt
package com.umc.hellodoctor.feature.drug.data.repository

import com.umc.hellodoctor.feature.drug.data.api.MedicineApi
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import com.umc.hellodoctor.feature.drug.domain.model.Medicine
import com.umc.hellodoctor.feature.drug.domain.repository.MedicineRepository
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val medicineApi: MedicineApi
) : MedicineRepository {

    override suspend fun searchMedicines(keyword: String): List<Medicine> {
        // Call API
        val response = medicineApi.searchMedicines(keyword)

        // Check success and extract result
        return if (response.success) {
            response.result.map { it.toDomain() }
        } else {
            throw Exception(response.message)
        }
    }

    override suspend fun getMedicine(medicineId: String): Medicine {
        val response = medicineApi.getMedicine(medicineId)

        return if (response.success) {
            response.result.toDomain()
        } else {
            throw Exception(response.message)
        }
    }
}

// Helper extension function for DTO -> Domain conversion
private fun MedicineSearchItem.toDomain(): Medicine =
    Medicine(
        id = this.id,
        name = this.name,
        ingredient = this.ingredient,
        dosage = this.dosage ?: "Unknown"
    )
```

**Rules**:
- Repository receives API client through constructor injection
- Always handle `BaseResponse` success check
- Transform DTOs to domain models with extension functions
- Throw exceptions with meaningful messages
- Put repository implementation in `data/repository/` package

## Domain Layer Repository Interface

```kotlin
// feature/drug/domain/repository/MedicineRepository.kt
package com.umc.hellodoctor.feature.drug.domain.repository

import com.umc.hellodoctor.feature.drug.domain.model.Medicine

interface MedicineRepository {
    suspend fun searchMedicines(keyword: String): List<Medicine>
    suspend fun getMedicine(medicineId: String): Medicine
}
```

**Rules**:
- Define contract in domain layer
- Expose only domain models
- Use `suspend` for coroutine operations
- Put repository interfaces in `domain/repository/` package

## AuthInterceptor

Automatically injects JWT tokens into requests:

```kotlin
// core/network/AuthInterceptor.kt
package com.umc.hellodoctor.core.network

import com.umc.hellodoctor.feature.auth.presentation.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = tokenManager.getAccessToken()
        val newRequest = originalRequest.newBuilder().apply {
            if (accessToken.isNotEmpty()) {
                addHeader("Authorization", "Bearer $accessToken")
            }
            addHeader("Content-Type", "application/json")
            addHeader("Accept", "application/json")
        }.build()

        return chain.proceed(newRequest)
    }
}
```

**How it works**:
1. Extracts access token from `TokenManager`
2. Adds `Authorization: Bearer <token>` header
3. Adds content-type headers
4. Proceeds with request

## Retrofit Configuration

`NetworkModule` is in `app/di/` and installed in `SingletonComponent`. It configures `OkHttpClient` (with `AuthInterceptor`), `Retrofit`, and registers all API services including `MedicineApi`.

→ See [hilt-di.md — App-level Modules](hilt-di.md#app-level-modules) for the canonical `NetworkModule` code example.

**Configuration**:
- Base URL: `BuildConfig.SERVER_BASE_URL` (from `gradle.properties`)
- Timeouts: All set to **30 seconds**
- Converter: Gson for JSON serialization
- Interceptors: AuthInterceptor for token injection

## Using APIs in ViewModels

ViewModels call APIs (or use cases) within `viewModelScope.launch`. Always update UI state (Loading, Success, Empty, Error) to represent async operation status.

→ See [ui-patterns.md — ViewModel Architecture](ui-patterns.md#viewmodel-architecture) for the canonical `DrugViewModel` and `DrugSearchUiState` example (including debounce and Empty state patterns).

**Rules**:
- Always use use cases instead of calling repository directly
- Handle exceptions with try-catch
- Update UI state (Loading, Success, Error)
- Use viewModelScope for coroutine lifecycle

## Error Handling

### BaseResponse Pattern

```kotlin
// Check success flag
if (response.success) {
    return response.result
} else {
    // Use message for error
    throw ApiException(response.code, response.message)
}
```

### Custom Exception

```kotlin
data class ApiException(
    val code: String,
    override val message: String
) : Exception(message)
```

### In ViewModel

```kotlin
try {
    val result = searchUseCase(keyword)
    updateUI(result)
} catch (e: ApiException) {
    showError("Server error: ${e.message}")
} catch (e: IOException) {
    showError("Network error")
} catch (e: Exception) {
    showError("Unknown error: ${e.message}")
}
```

## Adding New API Endpoints

**Step-by-step**:

1. **Define DTO** in `feature/[name]/data/model/`:
```kotlin
data class MyResponse(
    @SerializedName("field1") val field1: String,
    @SerializedName("field2") val field2: Int
)
```

2. **Add endpoint** to `feature/[name]/data/api/MyApi.kt`:
```kotlin
@GET("v1/endpoint")
suspend fun getMyData(): BaseResponse<MyResponse>
```

3. **Register** in `app/di/NetworkModule.kt`:
```kotlin
@Provides
@Singleton
fun provideMyApi(retrofit: Retrofit): MyApi =
    retrofit.create(MyApi::class.java)
```

4. **Implement** repository in `feature/[name]/data/repository/`:
```kotlin
override suspend fun getMyData(): MyDomainModel {
    val response = api.getMyData()
    return if (response.success) {
        response.result.toDomain()
    } else throw Exception(response.message)
}
```

5. **Create use case** in `feature/[name]/domain/usecase/`:
```kotlin
class GetMyDataUseCase @Inject constructor(
    private val repo: MyRepository
) {
    suspend operator fun invoke(): MyDomainModel = repo.getMyData()
}
```

6. **Register use case** in `feature/[name]/di/MyModule.kt`:
```kotlin
@Provides
@Singleton
fun provideGetMyDataUseCase(repo: MyRepository): GetMyDataUseCase =
    GetMyDataUseCase(repo)
```

7. **Use in ViewModel**:
```kotlin
viewModelScope.launch {
    try {
        val data = useCase()
        updateUI(data)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

## Request & Response Examples

### POST with Body

```kotlin
// API
@POST("v1/auth/login")
suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>

// DTO
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long
)

// Repository
override suspend fun login(email: String, password: String): LoginResponse {
    val request = LoginRequest(email, password)
    val response = api.login(request)
    return if (response.success) {
        response.result
    } else {
        throw ApiException(response.code, response.message)
    }
}
```

### Query Parameters

```kotlin
// API
@GET("v1/medicines/search")
suspend fun searchMedicines(
    @Query("name") keyword: String,
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 20
): BaseResponse<List<Medicine>>

// Usage
val results = api.searchMedicines(keyword = "aspirin", page = 1)
```

### Path Parameters

```kotlin
// API
@GET("v1/medicines/{id}")
suspend fun getMedicineById(
    @Path("id") medicineId: String
): BaseResponse<Medicine>

// Usage
val medicine = api.getMedicineById("123")
```

## Testing Network Layer

For comprehensive repository and API test examples (success and failure paths, Arrange-Act-Assert structure), see [testing.md — Unit Tests](testing.md#unit-tests).

## Best Practices

1. **Always use BaseResponse**: All endpoints return this wrapper
2. **Suspend functions**: All API calls use `suspend` for coroutines
3. **DTO to Domain mapping**: Transform in repository, not ViewModel
4. **Error handling**: Check `success` flag and throw on failure
5. **Timeout handling**: 30-second timeouts configured globally
6. **Token injection**: AuthInterceptor handles automatically
7. **Nullable fields**: Use `? = null` for optional JSON fields
8. **Meaningful names**: API interfaces and DTOs clearly reflect their purpose

## Related Documentation

- [Architecture](architecture.md) - Repository pattern and layer structure
- [Coding Conventions](coding-conventions.md) - Data class and naming styles
- [Hilt DI](hilt-di.md) - NetworkModule configuration
- [Testing](testing.md) - Repository and API testing patterns

---

**Last Updated**: 2026-03-31
