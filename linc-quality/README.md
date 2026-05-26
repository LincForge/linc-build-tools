# linc-quality convention plugin

Detekt + ktlint bundled with LINC defaults. Single plugin id, hard-fail by default.

## What you get

Apply `id("linc-quality")` and your project gains:

| Task | Behavior |
|---|---|
| `./gradlew detekt` | Static analysis with the bundled LINC config. Hard-fail on first violation. |
| `./gradlew ktlintCheck` | Kotlin style check via ktlint-gradle. Hard-fail on first violation. |
| `./gradlew detektBaseline` | Generate `detekt-baseline.xml` to suppress current violations during onboarding. |
| `./gradlew ktlintFormat` | Auto-format Kotlin sources. |

The bundled detekt config is harvested from `SpatialLearn/detekt.yml` and `Imagination/detekt.yml`. Override per-repo via `lincQuality { detektConfigPath.set("custom.yml") }`.

## Usage (gated composite include)

The composite `includeBuild("../linc-build-tools/linc-quality")` propagates to every downstream consumer (LINC playbook gotcha `feedback_composite_includebuild_propagates`). Gate it behind a Gradle property so downstream apps are unaffected by default.

In your KMP repo's `settings.gradle.kts`:

```kotlin
val qualityEnabled = providers.gradleProperty("linc.quality.enabled").orNull == "true"

if (qualityEnabled) {
    includeBuild("../linc-build-tools/linc-quality")
}
```

In your root or subproject `build.gradle.kts`:

```kotlin
plugins {
    id("linc-quality") apply false
}

subprojects {
    if (providers.gradleProperty("linc.quality.enabled").orNull == "true") {
        apply(plugin = "linc-quality")
    }
}
```

Run with `-Plinc.quality.enabled=true`:

```bash
./gradlew detekt ktlintCheck -Plinc.quality.enabled=true
```

## Per-repo overrides

```kotlin
lincQuality {
    // Soft-gate during onboarding — violations report but don't fail the build.
    ignoreFailures.set(true)

    // Override the bundled detekt config.
    detektConfigPath.set("$rootDir/config/detekt.yml")
}
```

## Versions

- detekt-gradle-plugin: `1.23.8`
- ktlint-gradle: `12.1.1`
- Kotlin Gradle Plugin: `2.0.21` (aligned with `linc-pitest`)
- Gradle wrapper: `8.12` (aligned with `linc-pitest`)
- JVM toolchain: 17 (for the plugin build itself; consumers control their own)

## Why a convention plugin

Without this plugin, every KMP repo would have to declare two `plugins {}` entries, configure their detekt extension, supply a config file, configure ktlint exclusions, and wire `ignoreFailures` consistently. Roughly 30-40 lines per repo, with drift inevitable. The convention plugin moves that into one shared place so the safety-nets `pillar-lint` waveform (`./gradlew detekt ktlintCheck`) is identical across every KMP repo.

## Local development

```bash
./gradlew test                  # ProjectBuilder unit tests
./gradlew publishToMavenLocal   # publishes com.linc:linc-quality:0.1.0
```

After publishing, consumers using `includeBuild` will pick up changes automatically on their next build.

## Future direction

Phase 2 will publish this to a real Maven artifact alongside `linc-pitest`, dropping the `includeBuild` requirement. See `LINC/docs/superpowers/specs/2026-05-26-ai-safety-nets-design.md` (pillar-lint section).
