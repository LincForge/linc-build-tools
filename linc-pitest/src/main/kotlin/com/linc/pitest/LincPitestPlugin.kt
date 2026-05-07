package com.linc.pitest

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

class LincPitestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("lincPitest", LincPitestExtension::class.java)
        ext.jvmToolchain.convention(21)
        ext.excludedTestFiles.convention(emptyList())

        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("info.solidsoft.pitest")
        project.repositories.run {
            google()
            mavenCentral()
        }

        project.afterEvaluate {
            configureToolchain(project, ext)
            configureProducerSync(project, ext)
            configurePitestDefaults(project, ext)
            configureSourceSets(project, ext)
        }
    }

    private fun configureToolchain(project: Project, ext: LincPitestExtension) {
        val toolchainVersion = ext.jvmToolchain.get()
        val java = project.extensions.getByType(JavaPluginExtension::class.java)
        java.toolchain.languageVersion.set(JavaLanguageVersion.of(toolchainVersion))
        java.targetCompatibility = JavaVersion.toVersion(toolchainVersion)
        java.sourceCompatibility = JavaVersion.toVersion(toolchainVersion)
    }
}
