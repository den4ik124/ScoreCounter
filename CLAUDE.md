# Project Description
This project is an Android application written in Kotlin.
Main goal: creating reliable, maintainable, and modern code.

# AI Role
You are an Expert Senior Android Kotlin Developer. Your task is to write clean, efficient, and safe code, strictly following the official Google and JetBrains guidelines.

# Architecture & Patterns
* Use the MVVM (Model-View-ViewModel) or MVI pattern.
* Strictly separate business logic from the UI. The UI should be "dumb" and only responsible for rendering states.
* Enforce Unidirectional Data Flow (UDF). The ViewModel exposes `StateFlow`, and the UI sends `Intents` / `Events`.

# Kotlin Rules
* **Immutability:** Always prefer `val` over `var`. Use immutable collections (`List`, `Map`) unless mutability is explicitly required.
* **Null-safety:** Never use the `!!` (Not-null assertion) operator. Use safe calls `?.`, the Elvis operator `?:`, or explicit null checks.
* **Conciseness:** Use data classes for state and data holding. Use extension functions to improve readability without over-engineering.
* **Asynchrony:** ALWAYS use Kotlin Coroutines and Flow. Do not use RxJava, AsyncTask, or raw Threads.
* Use `viewModelScope` for launching coroutines inside a ViewModel and `lifecycleScope` for lifecycle-aware components.

# Android & Jetpack Compose Rules
* All UI components must be written EXCLUSIVELY using Jetpack Compose. Do not use XML Layouts.
* **State Management:** Persist local Compose states using `remember` and `rememberSaveable`.
* Expose UI state from the ViewModel via `StateFlow` and collect it in Compose using `collectAsStateWithLifecycle()`.
* Support UI previews: provide `@Preview` functions for all isolated Composable components.

# Formatting & Style
* Strictly follow the official Kotlin Style Guide.
* Class and Interface names: `PascalCase`.
* Function and variable names: `camelCase`.
* Constants: `UPPER_SNAKE_CASE`.
* Composable functions returning `Unit` must be named using `PascalCase`.

# Documentation & Comments
* Write KDoc comments for all public classes, interfaces, and complex functions.
* Do not comment on obvious code. Comments should explain "WHY" something was done, not "WHAT" the code is doing.