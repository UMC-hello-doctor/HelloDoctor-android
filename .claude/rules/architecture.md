# Architecture Rules — Clean Architecture & Feature-based Design

## Table of Contents

- [Overview](#overview)
- [Core Principles](#core-principles)
- [Layer Structure in Detail](#layer-structure-in-detail)
  - [Data Layer](#data-layer)
  - [Domain Layer](#domain-layer)
  - [Presentation Layer](#presentation-layer)
- [Cross-feature Communication](#cross-feature-communication)
- [Testing Structure](#testing-structure)
- [Common Patterns](#common-patterns)
- [Important Rules](#important-rules)

## Overview

HelloDoctor-android follows **Clean Architecture** with **feature-based modular** organization. Each feature is self-contained and implements the three core layers: data, domain, and presentation.

For comprehensive project context and setup, see [CLAUDE.md](../CLAUDE.md).

## Core Principles

### 1. Dependency Direction

```
Presentation → Domain ← Data
     ↓
   (never goes up)
```

- **Presentation layer** depends on **Domain** (interfaces & models)
- **Data layer** implements **Domain** interfaces
- **Domain layer** is independent (no dependencies on other layers)
- No layer should depend on presentation

### 2. Feature Isolation

Each feature is self-contained:
- Features communicate through navigation, not direct imports
- Feature A should NOT import from feature B's implementation
- Cross-feature sharing goes through `core/` package

### 3. Single Responsibility

Each class has one reason to change:
- API calls → Api/Repository layer
- Business logic → Domain layer
- UI logic → ViewModel/Presentation layer

## Layer Structure in Detail

### Data Layer

**Location**: `feature/[name]/data/`

Responsibilities:
- Network API calls (Retrofit)
- Database operations (Room)
- Local file storage
- Data transformation (API DTO → Domain Model)

Structure:
```
feature/drug/data/
├── api/                  # Retrofit API interfaces
│   └── MedicineApi.kt
├── repository/           # Repository implementations
│   └── MedicineRepositoryImpl.kt
├── model/                # DTOs and API models
│   ├── MedicineSearchItem.kt
│   └── MedicineResponse.kt
├── database/             # Room DAOs and entities
│   ├── DrugPlanEntity.kt
│   ├── DrugPlanDao.kt
│   └── DrugDatabase.kt
└── service/              # Services (alarms, notifications)
    └── DrugAlarmManager.kt
```

**Key Rules**:
- API models (DTOs) stay in `data/model/`
- Never expose DTO to presentation layer
- Repository implementation uses DTOs internally, returns domain models to presentation
- Use `@Inject` constructor injection, registered in Hilt modules

Example:
```kotlin
// data/model/MedicineSearchItem.kt
data class MedicineSearchItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("ingredient")
    val ingredient: String
)

// data/repository/MedicineRepositoryImpl.kt
class MedicineRepositoryImpl @Inject constructor(
    private val medicineApi: MedicineApi
) : MedicineRepository {
    override suspend fun searchMedicines(keyword: String): List<Medicine> {
        val response = medicineApi.searchMedicines(keyword)
        return response.result.map { it.toDomain() }
    }
}

// Helper extension
private fun MedicineSearchItem.toDomain(): Medicine =
    Medicine(id, name, ingredient)
```

### Domain Layer

**Location**: `feature/[name]/domain/`

Responsibilities:
- Define repository interfaces (contracts)
- Define domain models (pure, UI-agnostic)
- Implement business logic through use cases

Structure:
```
feature/drug/domain/
├── repository/           # Repository interfaces
│   └── MedicineRepository.kt
├── model/                # Domain models
│   └── Medicine.kt
└── usecase/              # Use cases (business logic)
    ├── SearchMedicinesUseCase.kt
    └── GetFavoriteMedicinesUseCase.kt
```

**Key Rules**:
- Repository interfaces define the contract
- Domain models are plain Kotlin classes/data classes
- No references to UI frameworks or Android classes
- Use cases encapsulate business logic
- No imports from `data/` or `presentation/` packages

Example:
```kotlin
// domain/repository/MedicineRepository.kt
interface MedicineRepository {
    suspend fun searchMedicines(keyword: String): List<Medicine>
}

// domain/model/Medicine.kt
data class Medicine(
    val id: String,
    val name: String,
    val ingredient: String
)

// domain/usecase/SearchMedicinesUseCase.kt
class SearchMedicinesUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(keyword: String): List<Medicine> {
        return if (keyword.isBlank()) emptyList()
        else repository.searchMedicines(keyword.trim())
    }
}
```

### Presentation Layer

**Location**: `feature/[name]/presentation/`

Responsibilities:
- Display data using ViewModels
- Handle user interactions
- Manage UI state (loading, error, success)
- Navigate between screens

Structure:
```
feature/drug/presentation/
├── ui/                   # Fragments, Activities
│   └── DrugSearchFragment.kt
├── viewmodel/            # ViewModels
│   ├── DrugViewModel.kt
│   └── DrugPlanViewModel.kt
└── adapter/              # RecyclerView adapters
    └── MedicineListAdapter.kt
```

**Key Rules**:
- ViewModel receives dependencies (use cases or repositories) through constructor injection with `@HiltViewModel`
- Use LiveData or StateFlow for state management
- Fragment/Activity should be thin (delegate logic to ViewModel)
- Bind only domain models in UI, never DTOs
- UI state should be sealed class or simple state holder

→ For the canonical ViewModel pattern with UI state management: [ui-patterns.md — ViewModel Architecture](ui-patterns.md#viewmodel-architecture)

Example:
```kotlin
// presentation/ui/DrugSearchFragment.kt
class DrugSearchFragment : Fragment() {
    private val viewModel: DrugViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { medicines ->
            adapter.submitList(medicines)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DrugSearchUiState.Loading -> showLoadingDialog()
                is DrugSearchUiState.Success -> hideLoadingDialog()
                is DrugSearchUiState.Error -> showErrorMessage(state.message)
                DrugSearchUiState.Idle -> {} // No UI change
            }
        }
    }

    private fun setupListeners() {
        binding.searchButton.setOnClickListener {
            viewModel.searchMedicines(binding.searchInput.text.toString())
        }
    }
}
```

## Cross-feature Communication

When multiple features need to communicate:

### Option 1: Shared Domain Models
Create shared models in `core/` package if needed across features:
```
core/model/
├── CommonUser.kt
└── CommonResult.kt
```

### Option 2: Navigation Arguments
Pass data through navigation safe args (preferred):
```kotlin
// nav_graph.xml
<action android:id="@+id/action_to_detail"
    app:destination="@id/detailFragment"
    app:enterAnim="@anim/slide_in_right"
    app:exitAnim="@anim/slide_out_left">
    <argument android:name="medicineId" app:type="string" />
</action>

// In ViewModel
private fun navigateToDetail(medicineId: String) {
    findNavController().navigate(
        DrugSearchFragmentDirections.actionToDrugDetail(medicineId)
    )
}
```

### Option 3: Shared Repository
For data that multiple features need, create a shared repository in `core/`:
```
core/repository/
├── UserRepository.kt         # Manages user data
└── UserRepositoryImpl.kt      # Implementation
```

## Testing Structure

Each layer should have corresponding tests. See [testing.md](testing.md) for comprehensive testing guidelines.

```
app/src/test/java/com/umc/hellodoctor/feature/drug/
├── data/
│   ├── repository/MedicineRepositoryImplTest.kt
│   └── api/MedicineApiTest.kt
├── domain/
│   └── usecase/SearchMedicinesUseCaseTest.kt
└── presentation/
    └── viewmodel/DrugViewModelTest.kt
```

**Testing Rules**:
- Unit test data layer with mocked API
- Unit test domain layer independently
- Unit test ViewModel with mocked use cases
- Use JUnit 4 + Mockk for mocking
- Keep tests fast (< 100ms each)

→ For comprehensive test examples (repository, use case, ViewModel) with Arrange-Act-Assert structure: [testing.md — Unit Tests](testing.md#unit-tests)

## Common Patterns

> **Note**: `DrugRepositoryImpl` does not yet exist in production code. `DrugViewModel` currently injects `MedicineApi` directly. This pattern below represents the **intended target architecture** when the repository layer is added.

### Repository Pattern
```kotlin
// Interface in domain
interface DrugRepository {
    suspend fun searchMedicines(keyword: String): List<Medicine>
    suspend fun getMedicine(id: String): Medicine
}

// Implementation in data
class DrugRepositoryImpl @Inject constructor(
    private val api: MedicineApi,
    private val dao: DrugPlanDao
) : DrugRepository {
    override suspend fun searchMedicines(keyword: String): List<Medicine> {
        return api.searchMedicines(keyword).result.map { it.toDomain() }
    }

    override suspend fun getMedicine(id: String): Medicine {
        return api.getMedicine(id).result.toDomain()
    }
}
```

### ViewModel with LiveData
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    private val _state = MutableLiveData<UIState>()
    val state: LiveData<UIState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = UIState.Loading
            try {
                val data = useCase()
                _state.value = UIState.Success(data)
            } catch (e: Exception) {
                _state.value = UIState.Error(e)
            }
        }
    }
}
```

### Extension Functions for Transformation
```kotlin
// In data layer for DTO → Domain transformation
internal fun MedicineSearchItemDto.toDomain(): Medicine =
    Medicine(
        id = this.id,
        name = this.name,
        ingredient = this.ingredient
    )
```

## Important Rules

1. **No Circular Dependencies**: If A imports B, B must not import A
2. **No Presentation in Domain**: Domain layer must never import Android or presentation classes
3. **No Business Logic in UI**: All logic goes to ViewModel/UseCase
4. **Suspend Functions for Async**: Always use `suspend` for coroutine-based async operations
5. **Constructor Injection**: Always use constructor injection with `@Inject` and Hilt
6. **Single Responsibility**: One class = one reason to change
7. **SOLID Principles**: Especially Interface Segregation and Dependency Inversion

## Related Documentation

- [Coding Conventions](coding-conventions.md) - Code style and Kotlin idioms
- [API Integration](api-integration.md) - Network layer implementation
- [Hilt DI](hilt-di.md) - Dependency injection setup
- [UI Patterns](ui-patterns.md) - ViewModel and Fragment patterns
- [Testing](testing.md) - Testing each layer
- [Commit Conventions](commit-conventions.md) - Git and PR practices

---

**Last Updated**: 2026-03-31
