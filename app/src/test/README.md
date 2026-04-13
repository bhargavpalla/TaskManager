# TaskFlow Test Guide

This folder contains unit tests for domain and presentation logic.

## Test Stack

- JUnit4
- `kotlinx-coroutines-test`
- Turbine (Flow assertions)
- MockK (mocking + interaction verification)

## Test Structure

- `domain/usecase/TaskMutationUseCasesTest.kt`
  - `CreateTaskUseCase`
  - `UpdateTaskUseCase`
  - `DeleteTaskUseCase`
  - `RestoreTaskUseCase`
  - `DeleteTaskPermanentlyUseCase`

- `domain/usecase/TaskQueryUseCasesTest.kt`
  - `GetTasksUseCase`
  - `GetDeletedTasksUseCase`
  - `SearchTasksUseCase`
  - `FilterTasksUseCase`

- `domain/usecase/GroupTasksUseCaseTest.kt`
  - grouping correctness + no-overlap

- `domain/util/TaskDateUtilsTest.kt`
  - date classification behavior (`isToday`, `isThisWeek`, `isLater`, `isOverdue`)

- `presentation/task/TaskViewModelTest.kt`
  - search/debounce/distinct behavior
  - UI state mapping (Loading/Success/Empty/Error)
  - filter + grouping integration
  - action events
  - trash flow states

- `testutil/MainDispatcherRule.kt`
  - swaps `Dispatchers.Main` for coroutine tests

## Run Tests

From project root:

- All unit tests:
  - `./gradlew :app:testDebugUnitTest`

- Single class:
  - `./gradlew :app:testDebugUnitTest --tests "com.example.taskmanager.domain.usecase.GroupTasksUseCaseTest"`

- Single test method:
  - `./gradlew :app:testDebugUnitTest --tests "com.example.taskmanager.presentation.task.TaskViewModelTest.queryChangeTriggersSearchUseCase_afterDebounce"`

## Testing Principles Used

- Domain tests are pure Kotlin and Android-free.
- ViewModel tests mock use cases (do not re-test use case internals).
- Flow behavior is asserted with Turbine.
- Interaction contracts are verified with MockK (`verify`/`coVerify`).
