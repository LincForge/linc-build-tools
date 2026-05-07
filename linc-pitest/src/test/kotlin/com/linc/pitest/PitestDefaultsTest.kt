package com.linc.pitest

import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PitestDefaultsTest {

    private fun buildEvaluatedProject(rootPackage: String = "com.fake.lib"): ProjectInternal {
        val project = ProjectBuilder.builder().build() as ProjectInternal
        project.plugins.apply("linc-pitest")
        val ext = project.extensions.getByType(LincPitestExtension::class.java)
        ext.producerModule.set(":fake-producer")
        ext.rootPackage.set(rootPackage)
        project.evaluate()
        return project
    }

    @Test
    fun `targetClasses defaults to rootPackage wildcard`() {
        val project = buildEvaluatedProject()
        val pitest = project.extensions.getByType(PitestPluginExtension::class.java)
        assertEquals(setOf("com.fake.lib.*"), pitest.targetClasses.get())
    }

    @Test
    fun `targetTests defaults to rootPackage wildcard`() {
        // Gotcha 69b1fa7d: targetTests must be explicitly set or pitest finds 0 tests.
        val project = buildEvaluatedProject()
        val pitest = project.extensions.getByType(PitestPluginExtension::class.java)
        assertEquals(setOf("com.fake.lib.*"), pitest.targetTests.get())
    }

    @Test
    fun `avoidCallsTo includes Kotlin Intrinsics`() {
        // Gotcha 7483a5a3: Kotlin Intrinsics::* removals are equivalent mutants.
        val project = buildEvaluatedProject()
        val pitest = project.extensions.getByType(PitestPluginExtension::class.java)
        val avoidCallsTo = pitest.avoidCallsTo.get()
        assertTrue(avoidCallsTo.contains("kotlin.jvm.internal.Intrinsics"),
            "Expected kotlin.jvm.internal.Intrinsics in avoidCallsTo; got: $avoidCallsTo")
    }

    @Test
    fun `avoidCallsTo includes DebugMetadataKt`() {
        val project = buildEvaluatedProject()
        val pitest = project.extensions.getByType(PitestPluginExtension::class.java)
        val avoidCallsTo = pitest.avoidCallsTo.get()
        assertTrue(avoidCallsTo.contains("kotlin.coroutines.jvm.internal.DebugMetadataKt"),
            "Expected DebugMetadataKt in avoidCallsTo; got: $avoidCallsTo")
    }
}
