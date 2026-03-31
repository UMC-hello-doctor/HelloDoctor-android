# Testing Rules & Patterns

## Table of Contents

- [Test Structure](#test-structure)
- [Testing Tools](#testing-tools)
- [Unit Tests](#unit-tests)
- [Instrumentation Tests (Android)](#instrumentation-tests-android)
- [Best Practices](#best-practices)
- [Running Tests](#running-tests)
- [Coverage Requirements](#coverage-requirements)
- [Common Assertions](#common-assertions)
- [Mocking Patterns](#mocking-patterns)
- [Example Test Files](#example-test-files)

## Test Structure

Tests are organized by layer, mirroring the source structure:

```
app/src/
├── test/java/                        # Unit tests (JVM)
│   └── com/umc/hellodoctor/
│       └── feature/[name]/
│           ├── data/
│           │   ├── api/
│           │   │   └── MedicineApiTest.kt
│           │   └── repository/
│           │       └── MedicineRepositoryImplTest.kt
│           ├── domain/
│           │   └── usecase/
│           │       └── SearchMedicinesUseCaseTest.kt
│           └── presentation/
│               └── viewmodel/
│                   └── DrugViewModelTest.kt
│
└── androidTest/java/                 # Instrumentation tests (Android)
    └── com/umc/hellodoctor/
        └── integration/
            └── MyIntegrationTest.kt
```

## Testing Tools

### Dependencies
- **JUnit 4**: Testing framework
- **Mockk**: Kotlin mocking library
- **Coroutines Test**: Coroutine testing utilities
- **Hilt Testing**: Hilt for instrumentation tests
- **AssertJ**: Assertion library (for fluent assertions)

## Unit Tests

### 1. Repository Tests

> **Note**: `MedicineSearchItem` 의 실제 필드는 `medicineId`, `medicineName`, `entpName`, `medicineImage`, `efficacy` 이며 `@Parcelize Parcelable` 을 구현합니다. 아래 예제는 설명을 위해 단순화된 필드명을 사용합니다.

Test data layer transformations:

```kotlin
// feature/drug/data/repository/MedicineRepositoryImplTest.kt
class MedicineRepositoryImplTest {

    private lateinit var repository: MedicineRepositoryImpl
    private val medicineApi: MedicineApi = mockk()

    @Before
    fun setup() {
        repository = MedicineRepositoryImpl(medicineApi)
    }

    @Test
    fun `searchMedicines returns domain models on success`() = runTest {
        // Arrange
        val dtoList = listOf(
            MedicineSearchItem("1", "Aspirin", "acetylsalicylic acid"),
            MedicineSearchItem("2", "Ibuprofen", "ibuprofen")
        )
        val response = BaseResponse(
            success = true,
            code = "000",
            message = "Success",
            result = dtoList
        )
        every { runBlocking { medicineApi.searchMedicines("pain") } } returns response

        // Act
        val result = repository.searchMedicines("pain")

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("1")
        assertThat(result[0].name).isEqualTo("Aspirin")
        verify { runBlocking { medicineApi.searchMedicines("pain") } }
    }

    @Test
    fun `searchMedicines throws exception on failure`() = runTest {
        // Arrange
        val response = BaseResponse(
            success = false,
            code = "400",
            message = "Bad request",
            result = emptyList<MedicineSearchItem>()
        )
        every { runBlocking { medicineApi.searchMedicines("") } } returns response

        // Act & Assert
        assertThatThrownBy { runBlocking { repository.searchMedicines("") } }
            .isInstanceOf(Exception::class.java)
            .hasMessage("Bad request")
    }
}
```

**Rules**:
- Use `mockk()` for mocking API calls
- Test success and failure paths
- Verify API was called with correct parameters
- Use `runTest { }` for suspend functions
- Arrange-Act-Assert pattern

### 2. Use Case Tests

Test business logic:

```kotlin
// feature/drug/domain/usecase/SearchMedicinesUseCaseTest.kt
class SearchMedicinesUseCaseTest {

    private lateinit var useCase: SearchMedicinesUseCase
    private val repository: MedicineRepository = mockk()

    @Before
    fun setup() {
        useCase = SearchMedicinesUseCase(repository)
    }

    @Test
    fun `invoke returns empty list for blank keyword`() = runTest {
        // Act
        val result = useCase("   ")

        // Assert
        assertThat(result).isEmpty()
        verify(exactly = 0) { runBlocking { repository.searchMedicines(any()) } }
    }

    @Test
    fun `invoke trims keyword before calling repository`() = runTest {
        // Arrange
        val medicines = listOf(
            Medicine("1", "Aspirin", "acetylsalicylic acid", "500mg")
        )
        every { runBlocking { repository.searchMedicines("aspirin") } } returns medicines

        // Act
        val result = useCase("  aspirin  ")

        // Assert
        assertThat(result).isEqualTo(medicines)
        verify { runBlocking { repository.searchMedicines("aspirin") } }
    }

    @Test
    fun `invoke propagates repository exceptions`() = runTest {
        // Arrange
        val exception = RuntimeException("Network error")
        every { runBlocking { repository.searchMedicines(any()) } } throws exception

        // Act & Assert
        assertThatThrownBy { runBlocking { useCase("test") } }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Network error")
    }
}
```

**Rules**:
- Test business logic independently of data layer
- Mock repository to test use case logic only
- Test edge cases (empty input, null, exceptions)
- Verify parameters passed to dependencies

### 3. ViewModel Tests

> **Note**: The real `DrugViewModel` injects `MedicineApi` directly. See [ui-patterns.md#viewmodel-architecture](ui-patterns.md#viewmodel-architecture) for the current constructor signature.

Test state management:

```kotlin
// feature/drug/presentation/DrugViewModelTest.kt
class DrugViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DrugViewModel
    private val searchUseCase: SearchMedicinesUseCase = mockk()
    private val getPlansUseCase: GetDrugPlansUseCase = mockk()

    @Before
    fun setup() {
        viewModel = DrugViewModel(searchUseCase, getPlansUseCase)
    }

    @Test
    fun `searchMedicines updates UI state correctly`() = runTest {
        // Arrange
        val medicines = listOf(
            Medicine("1", "Aspirin", "acetylsalicylic acid", "500mg")
        )
        every { runBlocking { searchUseCase("aspirin") } } returns medicines

        // Act
        viewModel.searchMedicines("aspirin")

        // Assert - check loading state
        assertThat(viewModel.uiState.value).isInstanceOf(DrugSearchUiState.Loading::class.java)

        // Wait for coroutine
        advanceUntilIdle()

        // Assert - check success state
        assertThat(viewModel.uiState.value).isInstanceOf(DrugSearchUiState.Success::class.java)
        assertThat(viewModel.searchResults.value).isEqualTo(medicines)
    }

    @Test
    fun `searchMedicines updates UI with error state on failure`() = runTest {
        // Arrange
        val exception = RuntimeException("Network error")
        every { runBlocking { searchUseCase("aspirin") } } throws exception

        // Act
        viewModel.searchMedicines("aspirin")

        // Wait for coroutine
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(DrugSearchUiState.Error::class.java)
        val errorState = state as DrugSearchUiState.Error
        assertThat(errorState.message).contains("Network error")
    }

    @Test
    fun `updateInputText updates LiveData`() {
        // Act
        viewModel.updateInputText("test input")

        // Assert
        assertThat(viewModel.inputText.value).isEqualTo("test input")
    }
}
```

**Rules**:
- Use `InstantTaskExecutorRule` for LiveData testing
- Use `runTest { advanceUntilIdle() }` for coroutines
- Test state transitions (Idle → Loading → Success/Error)
- Verify UI observables are updated correctly

## Instrumentation Tests (Android)

Test with Android framework and Hilt-injected dependencies:

```kotlin
// app/src/androidTest/java/.../integration/DrugIntegrationTest.kt
@HiltAndroidTest
class DrugIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `full flow from API to ViewModel works`() {
        // Test with actual Hilt-injected dependencies
        // Note: Use test doubles for remote API in test modules
    }
}
```

See [hilt-di.md](hilt-di.md#testing-with-hilt) for Hilt testing patterns.

## Best Practices

### 1. Test Naming
Use clear, descriptive names:

```kotlin
// Good
@Test
fun `searchMedicines returns empty list for blank keyword`() { }

@Test
fun `searchMedicines trims whitespace from keyword`() { }

@Test
fun `updateInputText updates LiveData`() { }

// Bad
@Test
fun testSearch() { }

@Test
fun test1() { }
```

### 2. AAA Pattern (Arrange-Act-Assert)
```kotlin
@Test
fun myTest() {
    // Arrange - setup
    val expected = listOf(Medicine("1", "Aspirin", "...", "..."))
    every { runBlocking { repository.search("aspirin") } } returns expected

    // Act - execute
    val result = useCase("aspirin")

    // Assert - verify
    assertThat(result).isEqualTo(expected)
}
```

### 3. One Assert Per Test (When Possible)
```kotlin
// Good
@Test
fun `searchMedicines returns non-empty list`() { ... }

@Test
fun `searchMedicines calls repository with trimmed keyword`() { ... }

// Avoid
@Test
fun `searchMedicines works correctly`() {
    // Multiple assertions about different behaviors
}
```

### 4. Mock External Dependencies
```kotlin
// Good - mock API layer
private val api: MedicineApi = mockk()

// Good - provide real business logic
val useCase = SearchMedicinesUseCase(mockRepository)

// Bad - create actual HTTP calls in tests
val api = Retrofit.Builder()...
```

### 5. Use Fixtures for Repeated Setup
```kotlin
// In a shared test utils file
object TestData {
    val defaultMedicine = Medicine("1", "Aspirin", "acetylsalicylic acid", "500mg")
    val defaultResponse = BaseResponse(
        success = true,
        code = "000",
        message = "Success",
        result = listOf(defaultMedicine)
    )
}

// In test
@Test
fun test() {
    every { runBlocking { api.search("") } } returns TestData.defaultResponse
}
```

### 6. Test Error Cases
```kotlin
@Test
fun `repository throws on API failure`() { ... }

@Test
fun `ViewModel handles exceptions gracefully`() { ... }

@Test
fun `useCase returns empty list on error`() { ... }
```

## Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.umc.hellodoctor.feature.drug.*"

# Run instrumentation tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

## Coverage Requirements

Target coverage per layer:
- **Domain layer**: 80%+ (core business logic)
- **Data layer**: 70%+ (APIs and repositories)
- **Presentation layer**: 50%+ (harder to test, but try for ViewModels)

## Common Assertions

```kotlin
// Size/content
assertThat(list).hasSize(3)
assertThat(list).isEmpty()
assertThat(list).isNotEmpty()

// Equality
assertThat(actual).isEqualTo(expected)
assertThat(actual).isNotEqualTo(other)

// Boolean
assertThat(flag).isTrue()
assertThat(flag).isFalse()

// Strings
assertThat(string).contains("substring")
assertThat(string).startsWith("prefix")
assertThat(string).endsWith("suffix")

// Exceptions
assertThatThrownBy { function() }
    .isInstanceOf(Exception::class.java)
    .hasMessage("Expected message")

// Type checks
assertThat(obj).isInstanceOf(MyClass::class.java)
```

## Mocking Patterns

### Mockk Basics
```kotlin
// Create mock
val mock: MyClass = mockk()

// Setup return value
every { mock.getValue() } returns 42

// Verify call
verify { mock.getValue() }

// Verify call count
verify(exactly = 2) { mock.getValue() }

// Verify not called
verify(exactly = 0) { mock.getValue() }

// Argument matching
every { mock.function(any()) } returns true
every { mock.function("specific") } returns false
```

### Testing Suspend Functions
```kotlin
// Mock suspend function
every { runBlocking { api.search("test") } } returns result

// Or
coEvery { api.search("test") } returns result

// Verify suspend function
verify { runBlocking { api.search("test") } }

// Or
coVerify { api.search("test") }
```

## Example Test Files

See existing test structure for reference:
- Check `app/src/test/` for unit test examples
- Check `app/src/androidTest/` for instrumentation test examples

## Related Documentation

- [Architecture](architecture.md) - Layer structure and testing organization
- [API Integration](api-integration.md) - Network layer testing patterns
- [Hilt DI](hilt-di.md) - Dependency injection for tests
- [Coding Conventions](coding-conventions.md) - Code style in tests

---

**Last Updated**: 2026-03-31
