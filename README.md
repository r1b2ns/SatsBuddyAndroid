# SatsBuddy Android

Android companion app for the [Coinkite SATSCARD](https://satscard.com) — a physical Bitcoin bearer card that uses NFC to store and transfer funds slot by slot.

This project is the Android port of [SatsBuddy for iOS](https://github.com/reez/SatsBuddy) by [@reez](https://github.com/reez).

---

## What is SATSCARD?

[SATSCARD](https://satscard.com) is a product by [Coinkite](https://coinkite.com) — a physical NFC card that holds Bitcoin. Each card has up to 10 slots, each with its own private key. You load a slot with Bitcoin, hand the card to someone, and they tap it to sweep the funds. No app pairing, no Bluetooth, no battery — just NFC.

SatsBuddy lets you manage your SATSCARDs: scan them, view balances, receive Bitcoin, and sweep funds to any address.

---

## Architecture

The project follows **Clean Architecture** with the **MVVM** pattern, structured in three layers:

```
presentation/   →   Jetpack Compose screens + ViewModels
domain/         →   Use cases + repository interfaces (no Android deps)
data/           →   Repository implementations + data sources
```

### Presentation
- **Jetpack Compose** with Material3
- One `UiState` data class per screen, exposed via `StateFlow`
- `@HiltViewModel` for ViewModel injection
- **Type-safe Navigation** (Navigation Compose 2.8) using `@Serializable` route destinations

### Domain
- Pure Kotlin, zero Android dependencies
- Use cases encapsulate all business logic (`BuildPsbtUseCase`, `SignAndBroadcastUseCase`, `GetFeesUseCase`, etc.)
- Results wrapped in `kotlin.Result<T>`
- Typed error hierarchy via `AppError` sealed class (`IncorrectCvc`, `WrongCard`, `InsufficientFunds`, etc.)

### Data

| Source | Technology |
|---|---|
| Remote API | Retrofit + OkHttp + Kotlinx Serialization → [Mempool.space](https://mempool.space) |
| Local storage | Jetpack DataStore + [Google Tink](https://github.com/google/tink) (AES-256-GCM, Android Keystore) |
| NFC | Android NFC API + [rust-cktap](https://github.com/coinkite/rust-cktap) (JNI, in progress) |
| Bitcoin | [Bitcoin Dev Kit (BDK)](https://bitcoindevkit.org) (in progress) |

### Dependency Injection
**Dagger Hilt** throughout — `@HiltAndroidApp`, `@HiltViewModel`, `@Binds` modules.

---

## Bitcoin Dev Kit (BDK)

Transaction building and broadcasting is powered by [Bitcoin Dev Kit](https://bitcoindevkit.org) via the `bdk-android` bindings. BDK handles PSBT construction and wallet descriptor management. Integration is currently in progress (`BdkDataSource`).

---

## Project Structure

```
app/src/main/java/com/satsbuddy/
├── data/
│   ├── bitcoin/        # BDK data source
│   ├── local/          # DataStore + Tink encrypted storage
│   ├── nfc/            # NFC session manager + rust-cktap transport
│   ├── remote/         # Mempool.space API + DTOs
│   └── repository/     # Repository implementations
├── di/                 # Hilt modules (Network, Repository, Storage)
├── domain/
│   ├── model/          # Domain entities + AppError
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
└── presentation/
    ├── carddetail/
    ├── cardlist/
    ├── navigation/     # Type-safe routes
    ├── receive/
    ├── send/           # Multi-step send flow (destination → fee → review → sign)
    ├── slothistory/
    ├── slotlist/
    └── theme/
```

---

## Requirements

- Android 8.0+ (API 26)
- NFC-capable device
- Internet connection (Mempool.space API)

---

## Publishing `cktap-android` to Maven Local (manual step, for now)

The `cktap-android` library (Kotlin bindings for [rust-cktap](https://github.com/coinkite/rust-cktap)) is **not yet published to a public Maven repository**. The app consumes it as `org.bitcoindevkit:cktap-android:0.1.0-SNAPSHOT` from your **local Maven repo** (`~/.m2/repository`), so you must build and publish it locally before the app can compile.

### Prerequisites

- [Rust toolchain](https://rustup.rs/) (`cargo`, `rustup`)
- Android NDK (installed via Android Studio → SDK Manager → SDK Tools → NDK)
- The [rust-cktap](https://github.com/coinkite/rust-cktap) repository cloned as a sibling of this project:

```
parent/
├── SatsBuddyAndroid/
└── rust-cktap/
```

### Steps

1. Export the NDK path (adjust the version to match what you have installed):
   ```bash
   export ANDROID_NDK_ROOT=~/Library/Android/sdk/ndk/27.2.12479018
   ```
2. Build the native library and generate the Kotlin bindings:
   ```bash
   cd ../rust-cktap/cktap-android
   bash scripts/dev/build-dev-macos-aarch64.sh
   ```
   This compiles `libcktap_ffi.so` for `arm64-v8a` and generates `lib/src/main/kotlin/com/coinkite/cktap/cktap_ffi.kt` via UniFFI.
3. Publish the AAR to your local Maven repository:
   ```bash
   ./gradlew :lib:publishToMavenLocal
   ```
   This installs `org.bitcoindevkit:cktap-android:0.1.0-SNAPSHOT` into `~/.m2/repository`.
4. Build SatsBuddy normally:
   ```bash
   cd ../../SatsBuddyAndroid
   ./gradlew :app:assembleDebug
   ```

SatsBuddy's [`settings.gradle.kts`](settings.gradle.kts) declares `mavenLocal()` as a dependency source, so the app will resolve the library directly from `~/.m2/repository`. Repeat step 3 whenever you change the library — Gradle will pick up the refreshed snapshot on the next build.

> Future work: publish `cktap-android` to a public Maven repository (e.g. Maven Central) so this manual step can be removed.

---

## Related

- [SatsBuddy iOS](https://github.com/reez/SatsBuddy) — original iOS app by @reez
- [SATSCARD](https://satscard.com) — Coinkite's NFC Bitcoin bearer card
- [Bitcoin Dev Kit](https://bitcoindevkit.org) — Bitcoin wallet library
- [Mempool.space](https://mempool.space) — Bitcoin explorer and fee API
