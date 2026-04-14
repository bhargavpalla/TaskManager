#  Task Manager App

A modern Android task management application built using Kotlin, Jetpack Compose, and Clean Architecture,focusing on scalability, testability, and offline-first principles.

 Setup Instructions
 Prerequisites

- Android Studio (latest stable version)
- JDK 17+
- Gradle installed
  
###Steps

1. Clone the repository:
   git clone <your-repo-url>
2. Open the project in Android Studio
3. Sync Gradle
4. Run the app:
   
# Architecture Overview

The app follows Clean Architecture with MVVM pattern**
```
presentation → domain → data
```

###  Presentation Layer(No business logic here)

- Jetpack Compose UI
- ViewModels
- State management using StateFlow

### Domain Layer

- UseCases (business logic)
- Pure Kotlin (no Android dependencies)
- Easily testable

### Data Layer

- Repository implementations
- Local database (Room)
- Mappers (Entity ↔ Domain)


#  Design Decisions

### Why Clean Architecture?

- Separation of concerns
- Easier testing
- Scalability for large apps


### Why StateFlow?

- Reactive UI updates
- Thread-safe
- Works well with coroutines and flow operators

### Why UseCases?

- Encapsulate business logic
- Avoid business logic in ViewModels
- Improve reusability

---

#  Technologies & Libraries

| Technology        | Purpose              | Justification                            |
| ----------------- | -------------------- | ---------------------------------------- |
| Jetpack Compose   | UI                   | Declarative, modern UI toolkit           |
| Coroutines + Flow | Async handling       | Structured concurrency, Async streams    |
| Room DB           | Local storage        | Offline-first support                    |
| WorkManager       | Background tasks     | Guaranteed scheduled work                |
| MockK             | Testing              | Kotlin-first mocking library             |
| Turbine           | Flow testing         | Easy testing of StateFlow/Flow           |
| JUnit             | Test framework       | Standard testing support                 |

---

#  Assumptions Made

- Tasks are stored locally (no backend integration)
- Device timezone is used for date calculations
- No authentication



# Tools Used

- Android Studio
- Cursor for AI assistance in writing code including test cases
- Git


#  Known Limitations / Future Improvements

### Current Limitations

- No backend support
- Limited UI animations
- Basic filtering options
- No pagination (if dataset grows)

### Future Improvements

- Add backend sync
- Implement pagination
- Add advanced filters & sorting
- Introduce dependency injection (Hilt)

---

# Testing Approach & Coverage


- Unit tests for:

  - ViewModel
  - UseCases
  - Date & grouping logic

###  Tools Used

- JUnit
- MockK
- Turbine
- kotlinx-coroutines-test

###  Coverage Focus

- Business logic (high priority)
- ViewModel state transitions
- Flow emissions (debounce, search)

###  Run tests
~~~
./gradlew testDebugUnitTest
~~~
###  Generate coverage
```
./gradlew testDebugUnitTestCoverage
```
Test Coverage achieved - 86%


                                                  ---Wrap---
