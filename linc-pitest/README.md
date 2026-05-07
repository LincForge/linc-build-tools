# linc-pitest convention plugin

Pitest mutation testing for KMP libraries via the consumer-module pattern.

## Why this exists

`info.solidsoft.pitest` 1.15.0 cannot register tasks on KMP modules — its `withType(JavaPlugin)` callback never fires under KMP's `JavaBasePlugin`-only `jvm()` target. The standalone `kotlin("jvm")` plugin DOES apply `JavaPlugin`, so a sibling JVM-only Gradle module that depends on the KMP module's compiled JVM artifacts can run Pitest normally.

This plugin extracts the 75-line consumer-module pattern (proven on SensorKit during Phase 0.5) into a reusable convention so each new KMP lib needs ~10 lines instead of 75.

## Usage

In your KMP repo's `settings.gradle.kts`:

```kotlin
includeBuild("../LINC/systems/gradle-plugins/linc-pitest")
include(":sensorkit-pitest")
```

Create `<lib>-pitest/build.gradle.kts`:

```kotlin
plugins {
    id("linc-pitest")
}

lincPitest {
    producerModule.set(":sensorkit")
    rootPackage.set("com.lincmobile.sensorkit")
    // Optional: tests blocked by Kotlin internal-access cross-module limitations
    excludedTestFiles.set(listOf("**/motion/FusionHealthMonitorTest.kt"))
    // Optional: override default JVM toolchain (default 21)
    // jvmToolchain.set(17)
}

dependencies {
    // Producer's runtime deps — needed so producer's classes can load at test time.
    // Mirror what the producer's commonMain pulls in.
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
    // ...

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks.test { useJUnit() }
```

Run:

```bash
./gradlew :sensorkit-pitest:pitest
open sensorkit-pitest/build/reports/pitest/index.html
```

## Encoded gotchas

This plugin encodes four gotchas discovered during the Phase 0.5 spike. You don't need to know them, but if something breaks, here's the mapping:

| Reactor improvement | Gotcha | Plugin mitigation |
|---|---|---|
| `9d35cb07` | `additionalMutableCodePaths` alone gives 100% NO_COVERAGE — Pitest mutates one bytecode while tests load another | `syncProducerClasses` task copies producer's compiled JVM classes into consumer's `main/classes`; no project-dep on producer |
| `69b1fa7d` | Pitest silently runs 0 tests if `targetTests` is unset (default regex too narrow) | Plugin auto-sets `targetTests` to match `targetClasses` (the rootPackage wildcard) |
| `e2c5cd3a` | `-Xfriend-paths` doesn't grant cross-module `internal` access in Kotlin 2.3.20 | `excludedTestFiles` DSL toggle skips affected files; long-term fix is to elevate the helpers to `@VisibleForTesting` |
| `7483a5a3` | Many "surviving mutants" are Kotlin `Intrinsics::checkNotNullExpressionValue` removals (equivalent — no observable behavior change), inflating survivor count and lowering raw mutation score | Plugin sets `pitest.avoidCallsTo` to suppress mutations on `kotlin.jvm.internal.Intrinsics` and similar |

## Toolchain

Default JVM toolchain is **21**. Override via `jvmToolchain.set(17)` if needed. Mismatch with the producer module's toolchain produces a silent `UnsupportedClassVersionError` at test runtime — keep them aligned.

## Limitations

- iOS Native (`iosMain`) actuals are not mutated. Pitest is JVM-only. Per LINC's TDD tier table, iOS actuals stay smoke-test-only.
- Producer's runtime dependencies must be redeclared in the consumer module. A future enhancement could read the producer's `jvmRuntimeClasspath` automatically; not implemented here.
- Composite-build path is hardcoded relative to LINC. If your KMP repo isn't a sibling of LINC, adjust the `includeBuild` path or migrate the plugin to a published artifact.
