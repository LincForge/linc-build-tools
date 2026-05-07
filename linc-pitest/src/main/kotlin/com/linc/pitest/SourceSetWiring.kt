package com.linc.pitest

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun configureSourceSets(project: Project, ext: LincPitestExtension) {
    val producerPath = ext.producerModule.get().removePrefix(":")
    val producerJvmClassesDir = producerClassesDir(project, ext)

    val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
    sourceSets.named("test", org.gradle.api.Action {
        java.srcDirs(
            "../$producerPath/src/jvmTest/kotlin",
            "../$producerPath/src/commonTest/kotlin",
        )
        resources.srcDirs(
            "../$producerPath/src/jvmTest/resources",
            "../$producerPath/src/commonTest/resources",
        )
    })

    // Friend-paths so commonTest's `internal` accesses compile.
    // Note: Kotlin 2.3.20 doesn't fully honor this for cross-module internals
    // (see gotcha e2c5cd3a) — that's why excludedTestFiles is the escape hatch.
    project.tasks.named("compileTestKotlin", KotlinCompile::class.java, org.gradle.api.Action {
        dependsOn("${ext.producerModule.get()}:compileKotlinJvm")
        friendPaths.from(producerJvmClassesDir)
        compilerOptions.freeCompilerArgs.add("-Xfriend-paths=${producerJvmClassesDir.absolutePath}")
        // Apply excluded test files glob list
        ext.excludedTestFiles.orNull?.forEach { glob ->
            exclude(glob)
        }
    })
}
