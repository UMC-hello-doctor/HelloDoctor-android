# UI Patterns & ViewModel Guide

## Table of Contents

- [Overview](#overview)
- [ViewModel Architecture](#viewmodel-architecture)
- [Fragment Implementation](#fragment-implementation)
- [RecyclerView Adapter](#recyclerview-adapter)
- [Dialog Fragments](#dialog-fragments)
- [UI State Management Patterns](#ui-state-management-patterns)
- [Handling Lifecycle](#handling-lifecycle)
- [Best Practices](#best-practices)
- [Extension Functions for Common Operations](#extension-functions-for-common-operations)

## Overview

The presentation layer follows Android best practices with:
- **ViewBinding** for type-safe UI binding
- **ViewModel** for state management
- **LiveData/StateFlow** for observable state
- **Fragment-based navigation** with Navigation Component

For coding conventions, see [coding-conventions.md](coding-conventions.md).

## ViewModel Architecture

### Basic ViewModel Structure

```kotlin
// feature/drug/presentation/viewmodel/DrugViewModel.kt
@HiltViewModel
class DrugViewModel @Inject constructor(
    private val medicineApi: MedicineApi
) : ViewModel() {

    // Private mutable state
    private val _inputText = MutableLiveData<String>("")
    val inputText: LiveData<String> get() = _inputText

    private val _searchResults = MutableLiveData<List<MedicineSearchItem>>(emptyList())
    val searchResults: LiveData<List<MedicineSearchItem>> get() = _searchResults

    private val _uiState = MutableLiveData<DrugSearchUiState>(DrugSearchUiState.Idle)
    val uiState: LiveData<DrugSearchUiState> get() = _uiState

    private var searchJob: Job? = null

    fun updateInputText(text: String) {
        if (_inputText.value != text) _inputText.value = text
    }

    fun searchMedicines(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) {
            searchJob?.cancel()
            _searchResults.value = emptyList()
            _uiState.value = DrugSearchUiState.Idle
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = DrugSearchUiState.Loading
            delay(300) // debounce: wait 300ms after last keystroke
            try {
                val response = medicineApi.searchMedicines(trimmed)
                if (response.success) {
                    val items = response.result
                    _searchResults.value = items
                    _uiState.value = if (items.isEmpty()) {
                        DrugSearchUiState.Empty
                    } else {
                        DrugSearchUiState.Success(items.size)
                    }
                } else {
                    _uiState.value = DrugSearchUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = DrugSearchUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }
}

// UI State Definition
sealed class DrugSearchUiState {
    object Idle : DrugSearchUiState()
    object Loading : DrugSearchUiState()
    object Empty : DrugSearchUiState()
    data class Success(val count: Int) : DrugSearchUiState()
    data class Error(val message: String) : DrugSearchUiState()
}
```

**Rules**:
- Annotate with `@HiltViewModel` and `@Inject` constructor
- Use backing fields (`_`) for mutable LiveData
- Expose read-only LiveData properties
- Use sealed classes for UI state
- Debounce rapid calls with `Job?.cancel()` + `delay()` for better UX

> **Note**: `DrugViewModel` currently injects `MedicineApi` directly rather than through a use case layer. When adding a repository abstraction, follow the full use case chain pattern shown in [architecture.md](architecture.md#domain-layer).

### ViewModel with Coroutines

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> get() = _messages

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                val response = chatUseCase.sendMessage(text)
                updateMessageList(response)
            } catch (e: IOException) {
                handleNetworkError()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // Handle timeout
    fun fetchWithTimeout() {
        viewModelScope.launch {
            try {
                val result = withTimeoutOrNull(5000L) {
                    chatUseCase.fetchMessages()
                }
                result?.let { updateMessageList(it) }
                    ?: run { handleTimeout() }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // Debounce for search-like operations
    private var searchJob: Job? = null

    fun debounceSearch(keyword: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)  // Wait 300ms after user stops typing
            val results = chatUseCase.search(keyword)
            _messages.value = results
        }
    }
}
```

**Rules**:
- Always use `viewModelScope.launch` (respects lifecycle)
- Handle different exception types separately
- Use `withTimeoutOrNull` for timeout handling
- Debounce rapid calls with `cancel()` + `delay()`

## Fragment Implementation

### Fragment with ViewBinding

```kotlin
// feature/drug/presentation/ui/DrugSearchFragment.kt
class DrugSearchFragment : Fragment() {

    private var _binding: FragmentDrugSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DrugViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrugSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
        setupRecyclerView()
    }

    private fun setupListeners() {
        binding.searchButton.setOnClickListener {
            val keyword = binding.searchInput.text.toString()
            viewModel.searchMedicines(keyword)
        }

        binding.searchInput.addTextChangedListener { text ->
            viewModel.updateInputText(text.toString())
        }
    }

    private fun setupObservers() {
        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { medicines ->
            adapter.submitList(medicines)
        }

        // Observe UI state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                DrugSearchUiState.Idle -> hideProgressBar()
                DrugSearchUiState.Loading -> showProgressBar()
                DrugSearchUiState.Success -> {
                    hideProgressBar()
                    showSuccessMessage()
                }
                is DrugSearchUiState.Error -> {
                    hideProgressBar()
                    showErrorMessage(state.message)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MedicineListAdapter { medicine ->
            onMedicineSelected(medicine)
        }
        binding.medicineList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DrugSearchFragment.adapter
        }
    }

    private fun onMedicineSelected(medicine: Medicine) {
        val action = DrugSearchFragmentDirections
            .actionDrugSearchToMedicineDetail(medicine.id)
        findNavController().navigate(action)
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSuccessMessage() {
        Toast.makeText(requireContext(), "Search completed", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        binding.errorMessage.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**Rules**:
- Always use ViewBinding (not findViewById)
- Initialize binding in `onCreateView`, null it in `onDestroyView`
- Call `setupListeners()` and `setupObservers()` in `onViewCreated`
- Observe with `viewLifecycleOwner` (not fragment itself)
- Use `when` statements for sealed class state
- Clean up references in `onDestroyView()`

### Fragment Navigation

```kotlin
// Send data to next fragment
private fun navigateToDetail(medicineId: String) {
    val action = DrugSearchFragmentDirections
        .actionDrugSearchToMedicineDetail(medicineId)
    findNavController().navigate(action)
}

// Receive data in detail fragment
class MedicineDetailFragment : Fragment() {
    private val args: MedicineDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val medicineId = args.medicineId
        viewModel.loadMedicine(medicineId)
    }
}
```

## RecyclerView Adapter

### ListAdapter Pattern (Preferred)

```kotlin
// feature/drug/presentation/adapter/MedicineListAdapter.kt
class MedicineListAdapter(
    private val onItemClick: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMedicineBinding,
        private val onItemClick: (Medicine) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) {
            binding.apply {
                medicineNameText.text = medicine.name
                medicineIngredientText.text = medicine.ingredient
                medicineDosageText.text = medicine.dosage

                root.setOnClickListener {
                    onItemClick(medicine)
                }
            }
        }
    }

    // Diff callback for efficient updates
    class DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem == newItem
    }
}
```

**Rules**:
- Extend `ListAdapter` for automatic diff updates
- Use ViewBinding in adapter
- Implement `DiffUtil.ItemCallback` for performance
- Pass click listeners as constructor parameters

### Submitting List

```kotlin
// In Fragment
viewModel.medicines.observe(viewLifecycleOwner) { medicines ->
    adapter.submitList(medicines)
}
```

## Dialog Fragments

```kotlin
class ConfirmDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Confirm Action")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("No", null)
            .create()
    }

    private fun onConfirm() {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(RESULT_KEY to true)
        )
    }

    companion object {
        const val REQUEST_KEY = "confirm_dialog"
        const val RESULT_KEY = "confirmed"
    }
}

// In parent fragment
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ConfirmDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val confirmed = result.getBoolean(ConfirmDialog.RESULT_KEY)
            if (confirmed) {
                performAction()
            }
        }
    }
}
```

## UI State Management Patterns

### Simple State
```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}
```

### Complex State with Multiple Properties
```kotlin
data class DetailUiState(
    val isLoading: Boolean = false,
    val data: Medicine? = null,
    val error: String? = null,
    val isFavorite: Boolean = false
)

// Usage
private val _uiState = MutableLiveData<DetailUiState>(DetailUiState())
val uiState: LiveData<DetailUiState> get() = _uiState

fun toggleFavorite() {
    _uiState.value = _uiState.value?.copy(isFavorite = true)
}
```

### Events vs State

```kotlin
// For one-time events (navigation, toast)
private val _navigationEvent = MutableLiveData<Event<String>>()
val navigationEvent: LiveData<Event<String>> = _navigationEvent

fun navigateToDetail(id: String) {
    _navigationEvent.value = Event(id)
}

// Event wrapper for one-time consumption
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }
}

// In Fragment
viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
    event.getContentIfNotHandled()?.let { medicineId ->
        navigateToDetail(medicineId)
    }
}
```

## Handling Lifecycle

### Safe Operations with Lifecycle

```kotlin
// Good: respects lifecycle
viewModel.data.observe(viewLifecycleOwner) { data ->
    updateUI(data)
}

// Bad: never receives updates if fragment is not in foreground
viewModel.data.observe(this) { data ->
    updateUI(data)
}

// For custom lifecycle operations
lifecycle.addObserver(object : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        // Start listening
    }

    override fun onStop(owner: LifecycleOwner) {
        // Stop listening
    }
})
```

## Best Practices

### 1. Keep Fragments Thin
Move logic to ViewModel, not Fragment:
```kotlin
// Good - ViewModel handles logic
viewModel.searchMedicines(keyword)

// Bad - Fragment handles logic
val medicines = medicineApi.search(keyword)
```

### 2. Use Sealed Classes for State
```kotlin
// Good - exhaustive when
when (state) {
    UiState.Loading -> showLoading()
    UiState.Success -> showData()
    is UiState.Error -> showError(state.message)
}

// Bad - not exhaustive
if (state == UiState.Loading) showLoading()
```

### 3. Observe with Lifecycle Owner
```kotlin
// Good
viewModel.data.observe(viewLifecycleOwner) { ... }

// Bad
viewModel.data.observe(this) { ... }
```

### 4. Type-safe Navigation
```kotlin
// Good - safe args
val action = DetailFragmentDirections.actionToDetail(id)
findNavController().navigate(action)

// Bad - string-based
findNavController().navigate(R.id.detailFragment, bundleOf("id" to id))
```

### 5. Handle View Lifecycle Properly
```kotlin
// Always null binding in onDestroyView
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

## Extension Functions for Common Operations

```kotlin
// Reusable extension for showing toast
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// Fragment extension
fun Fragment.showToast(message: String) {
    requireContext().showToast(message)
}

// Usage
showToast("Action completed")
```

## Related Documentation

- [Architecture](architecture.md) - Clean Architecture patterns
- [Coding Conventions](coding-conventions.md) - Kotlin code style
- [Hilt DI](hilt-di.md) - Dependency injection setup
- [Testing](testing.md) - Testing patterns for ViewModels

---

**Last Updated**: 2026-03-31
