package com.linc.pitest

import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.Project

internal fun configurePitestDefaults(project: Project, ext: LincPitestExtension) {
    val pitest = project.extensions.getByType(PitestPluginExtension::class.java)
    val rootPkg = ext.rootPackage.get()
    val wildcard = "$rootPkg.*"

    pitest.targetClasses.set(setOf(wildcard))
    // Gotcha 69b1fa7d: targetTests must be explicitly set or pitest finds 0 tests.
    pitest.targetTests.set(setOf(wildcard))

    pitest.threads.set(4)
    pitest.outputFormats.set(setOf("HTML", "XML"))
    pitest.timestampedReports.set(false)

    // Gotcha 7483a5a3: Kotlin Intrinsics::* removals are equivalent mutants.
    // VOID_METHOD_CALLS removing checkNotNullExpressionValue/etc. is the dominant
    // equivalent-mutant pattern and inflates survivor counts non-behaviorally.
    // Pitest's avoidCallsTo lets us tell it to never mutate calls into a class.
    pitest.avoidCallsTo.set(setOf(
        "kotlin.jvm.internal.Intrinsics",
        "kotlin.coroutines.jvm.internal.DebugMetadataKt",
    ))
}
