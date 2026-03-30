---
name: test
description: Generates unit tests for a given Kotlin class in HelloDoctor-android. Supports ViewModel, Repository, and UseCase tests. Usage: /test <file path or class name>
---

You will write unit tests for a Kotlin class in HelloDoctor-android.

The target class path or name follows after this prompt.

## Step 1 — Read the Target Class

Read the specified file fully to understand:
- Constructor dependencies (what to mock)
- Public methods and their behavior
- State exposed (LiveData, StateFlow, etc.)
- Edge cases implied by the implementation

## Step 2 — Read Existing Tests for Patterns

Glob for `**/test/**/*.kt` and read 1–2 similar existing tests to match:
- Test framework (JUnit4/JUnit5, Kotest)
- Mocking library (Mockito, MockK)
- Coroutine test setup (`TestCoroutineDispatcher`, `runTest`)
- Assertion style

## Step 3 — Determine Test Type and Location

| Class type | Test location | Approach |
|------------|---------------|----------|
| ViewModel | `test/` | Mock repository, test state emissions |
| Repository | `test/` | Mock API/DAO, test mapping and error handling |
| UseCase | `test/` | Mock repository, test business logic |
| Utility / Extension | `test/` | Pure input/output tests |

## Step 4 — Write Tests

Cover:
1. **Happy path**: normal successful execution
2. **Error path**: API error, DB error, null input
3. **Edge cases**: empty list, boundary values, rapid calls

For ViewModel tests:
- Use `TestCoroutineDispatcher` or `UnconfinedTestDispatcher`
- Add `InstantTaskExecutorRule` for LiveData
- Verify state changes in sequence

For Repository tests:
- Mock the API to return success and error responses
- Verify correct mapping from DTO → domain model
- Verify error is wrapped correctly

## Step 5 — Output

Write the test file to the correct location under `app/src/test/` or `app/src/androidTest/`.
List what each test covers and any mocking assumptions made.
