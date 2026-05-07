# linc-build-tools

Shared Gradle convention plugins consumed by LINC's KMP libraries and apps via composite builds.

## Plugins

| Plugin | Purpose |
|---|---|
| [`linc-pitest/`](./linc-pitest/) | Pitest mutation testing for KMP libraries via the consumer-module pattern |

## How consumers use this repo

In a consumer KMP library's `settings.gradle.kts`, opt in via Gradle property:

```kotlin
if (providers.gradleProperty("linc.pitest.enabled").orNull == "true") {
    includeBuild("../linc-build-tools/linc-pitest")
    include(":<lib>-pitest")
}
```

Then run with `-Plinc.pitest.enabled=true`:

```bash
./gradlew :<lib>-pitest:pitest -Plinc.pitest.enabled=true
```

The conditional ensures downstream apps that consume your library via composite build (`includeBuild("../<YourLib>")`) don't need to check out `linc-build-tools` themselves.

## Why a dedicated repo

Build tooling lives separately from the LINC monorepo (which is Python/markdown) so:
- KMP-library CI checkouts stay small (just this repo, not all of LINC).
- Build infrastructure has its own clean release lifecycle.
- New convention plugins (Detekt rules, KSP setups, Kover tiers) drop in as siblings.

## Future direction

Phase 2 of the mutation-testing rollout will publish these plugins to a Maven artifact, dropping the `includeBuild` requirement entirely. See `LINC/docs/superpowers/specs/2026-05-06-mutation-testing-design.md`.
