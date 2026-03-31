# Coding Conventions — Kotlin Style Guide

## Table of Contents

- [File Organization](#file-organization)
- [Naming Conventions](#naming-conventions)
- [Code Style](#code-style)
- [Android-Specific Conventions](#android-specific-conventions)
- [Formatting Rules](#formatting-rules)
- [Sealed Classes for State](#sealed-classes-for-state)
- [Hilt-Specific Conventions](#hilt-specific-conventions)
- [Code Quality Tools](#code-quality-tools)

## File Organization

### Import Organization
```kotlin
// 1. Package declaration
package com.umc.hellodoctor.feature.drug.presentation

// 2. Imports (organized by: android > androidx > com > org > java > javax)
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.drug.domain.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

// 3. Class definition
class MyClass { ... }
```

### File Naming
- **Classes/Interfaces**: PascalCase (e.g., `DrugViewModel.kt`, `MedicineRepository.kt`)
- **Functions/Variables**: camelCase (e.g., `searchMedicines()`, `inputText`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `REQUEST_TIMEOUT`)
- **Packages**: lowercase, hyphen-separated (e.g., `com.umc.hellodoctor.feature.drug.data`)

## Naming Conventions

### Class Names
```kotlin
// Repository
class MedicineRepositoryImpl : MedicineRepository

// ViewModel
class DrugViewModel : ViewModel()

// Fragment
class DrugSearchFragment : Fragment()

// API Interface
interface MedicineApi

// DAO
interface DrugPlanDao

// Model/Entity
data class Medicine(...)
data class MedicineSearchItem(...)  // @Parcelize Parcelable — see api-integration.md#defining-apis
class MedicineEntity(...)

// State (sealed class)
sealed class DrugSearchUiState {
    object Loading : DrugSearchUiState()
    data class Success(val data: List<Medicine>) : DrugSearchUiState()
    data class Error(val throwable: Throwable) : DrugSearchUiState()
}

// Adapter
class MedicineListAdapter : ListAdapter<Medicine, MedicineListAdapter.ViewHolder>(...)
```

### Variable Names
```kotlin
// LiveData
private val _searchResults = MutableLiveData<List<Medicine>>()
val searchResults: LiveData<List<Medicine>> get() = _searchResults

// StateFlow
private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Private backing field
private var _internalValue: String = ""

// Regular variable
val medicines: List<Medicine> = emptyList()
var inputText: String = ""
```

### Function Names
```kotlin
// Action functions (verbs)
fun searchMedicines(keyword: String) { }
fun loadData() { }
fun handleError(exception: Exception) { }

// Boolean functions
fun isValid(): Boolean { }
fun isEmpty(): Boolean { }
fun shouldRefresh(): Boolean { }

// Getter-like functions (no "get" prefix for simple accessors)
fun getFullName(): String { }  // Only if complex logic
val fullName: String           // Prefer property

// Extension functions
fun String.isEmailValid(): Boolean { }
fun View.showToast(message: String) { }
fun <T> List<T>.getOrEmpty(): List<T> { }
```

## Code Style

### Kotlin Properties vs Backing Fields

**Prefer properties for simple accessors:**
```kotlin
// Good
class MyClass {
    val title: String = "Hello"
    var count: Int = 0
}

// Avoid
class MyClass {
    private val _title: String = "Hello"
    val title: String get() = _title
}
```

**Use backing fields only when exposing mutable state:**
```kotlin
// Good (LiveData pattern)
class MyViewModel {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> get() = _state

    fun updateState(newState: State) {
        _state.value = newState
    }
}
```

### Data Classes
```kotlin
// Good: use data classes for models
data class Medicine(
    val id: String,
    val name: String,
    val ingredient: String
)

// Good: one property per line for readability
data class MedicineSearchRequest(
    val keyword: String,
    val pageNumber: Int = 1,
    val pageSize: Int = 20
)

// Bad: multiple properties per line
data class Medicine(val id: String, val name: String, val ingredient: String)
```

### Function Formatting

**Single-line functions:**
```kotlin
fun isEmpty(): Boolean = list.isEmpty()
fun getCount(): Int = items.size
```

**Multi-line functions:**
```kotlin
suspend fun searchMedicines(keyword: String): List<Medicine> {
    if (keyword.isBlank()) return emptyList()

    return try {
        api.searchMedicines(keyword).result.map { it.toDomain() }
    } catch (e: Exception) {
        emptyList()
    }
}
```

### Lambda Expressions
```kotlin
// Good: single parameter
items.map { it.name }

// Good: implicit 'it'
items.filter { it.isValid() }

// Good: named parameter
viewModel.updateState(newState = state)

// Named when multiple or non-obvious
users.forEach { user ->
    println(user.name)
}
```

### Null Handling
```kotlin
// Safe call operator for nullable types
val length: Int? = text?.length

// Elvis operator with default
val count: Int = items?.size ?: 0

// Non-null assertion (avoid when possible)
val name = user!!.name  // Only when 100% sure it's not null

// Use let for null checks
data?.let { notNullData ->
    processData(notNullData)
}

// Use when with smart casting
when (state) {
    is Success -> handleSuccess(state.data)
    is Error -> handleError(state.exception)
}
```

## Android-Specific Conventions

### ViewBinding
```kotlin
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### LiveData/StateFlow Usage
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    // LiveData pattern (older projects)
    private val _data = MutableLiveData<List<Item>>()
    val data: LiveData<List<Item>> = _data

    // StateFlow pattern (preferred)
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = useCase()
                _data.value = result
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e)
            }
        }
    }
}
```

### Coroutine Usage
```kotlin
class MyViewModel : ViewModel() {
    fun fetchData() {
        // Always use viewModelScope in ViewModel
        viewModelScope.launch {
            try {
                val result = api.getData()
                updateUI(result)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun fetchWithTimeout() {
        viewModelScope.launch {
            try {
                // Use withTimeoutOrNull for timeout handling
                val result = withTimeoutOrNull(5000L) {
                    api.getData()
                }
                result?.let { updateUI(it) }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}

class MyRepository @Inject constructor(
    private val api: MyApi
) {
    // Suspend functions for async operations
    suspend fun loadData(): List<Item> {
        return api.getItems().result.map { it.toDomain() }
    }
}
```

### String Resources
```kotlin
// Always use string resources, never hardcode UI strings
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Good: use string resources
        binding.titleTextView.text = getString(R.string.drug_search_title)

        // Bad: hardcoded string
        binding.titleTextView.text = "Search Medicines"
    }
}
```

### Constants
```kotlin
// Global constants in companion object
class MyClass {
    companion object {
        private const val REQUEST_TIMEOUT = 30L
        private const val PAGE_SIZE = 20
        private const val TAG = "MyClass"
    }
}

// Or in a separate Constants file
object AppConstants {
    const val REQUEST_TIMEOUT = 30L
    const val PAGE_SIZE = 20
}

object DrugConstants {
    const val SEARCH_DELAY_MS = 300L
    const val MAX_RESULTS = 100
}
```

## Formatting Rules

### Line Length
Maximum 100 characters per line. Use line wrapping for longer lines:

```kotlin
// Bad: over 100 chars
fun handleComplexOperation(parameter1: String, parameter2: String, parameter3: String) { }

// Good: break to multiple lines
fun handleComplexOperation(
    parameter1: String,
    parameter2: String,
    parameter3: String
) { }
```

### Indentation
- Use **4 spaces** (not tabs)
- Consistent indentation for continuation lines

```kotlin
val result = myObject
    .methodOne()
    .methodTwo()
    .methodThree()
```

### Comments
```kotlin
// Single-line comment for brief explanations
val apiTimeout = 30L  // in seconds

// Use doc comments for public APIs
/**
 * Searches for medicines by keyword.
 *
 * @param keyword the search term
 * @return list of matching medicines, empty if not found
 */
suspend fun searchMedicines(keyword: String): List<Medicine>

// Avoid commented-out code; use version control instead
// val oldImplementation = ...
```

## Hilt-Specific Conventions

### Constructor Injection
```kotlin
@HiltViewModel
class DrugViewModel @Inject constructor(
    private val searchUseCase: SearchMedicinesUseCase,
    private val getMedicineUseCase: GetMedicineUseCase
) : ViewModel()

class MedicineRepositoryImpl @Inject constructor(
    private val medicineApi: MedicineApi,
    private val dragPlanDao: DrugPlanDao
) : MedicineRepository
```

### Module Definition
```kotlin
// Currently provides DrugDatabase + DrugPlanDao only. Below shows target pattern.
@Module
@InstallIn(SingletonComponent::class)
object DrugModule {
    @Provides
    @Singleton
    fun provideMedicineRepository(
        medicineApi: MedicineApi,
        dragPlanDao: DrugPlanDao
    ): MedicineRepository = MedicineRepositoryImpl(medicineApi, dragPlanDao)
}
```

## Sealed Classes for State

```kotlin
// Good: sealed class for exhaustive when
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Usage
when (state) {
    AuthState.Idle -> {}
    AuthState.Loading -> showProgressBar()
    is AuthState.Success -> navigateToHome(state.user)
    is AuthState.Error -> showError(state.message)
}
```

## Type Aliases for Complex Types
```kotlin
typealias MedicineList = List<Medicine>
typealias SearchResult = Result<MedicineList>

// Usage becomes clearer
suspend fun search(keyword: String): SearchResult
```

## Code Quality Tools

### ktlint
This project uses **ktlint** for automatic Kotlin formatting:

```bash
./gradlew ktlintCheck        # Check formatting
./gradlew ktlintFormat       # Auto-format code
```

### detekt
Static code analysis for code smells:

```bash
./gradlew detekt             # Run analysis
```

Common issues detected:
- Unused imports
- Long methods
- High complexity
- Naming violations

### Code Reviews
When submitting PRs:
1. Run `./gradlew ktlintCheck` before pushing
2. Ensure no detekt warnings
3. Keep functions under 20 lines when possible
4. Use descriptive names
5. Add tests for new logic

## Related Documentation

- [Architecture](architecture.md) - Layer structure and design patterns
- [API Integration](api-integration.md) - Network layer specifics
- [UI Patterns](ui-patterns.md) - Android-specific conventions
- [Hilt DI](hilt-di.md) - Hilt annotation usage
- [Testing](testing.md) - Test naming conventions
- [Commit Conventions](commit-conventions.md) - Code quality in commits

---

**Last Updated**: 2026-03-31
