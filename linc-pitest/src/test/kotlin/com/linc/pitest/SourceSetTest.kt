package com.linc.pitest

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * Tests that the plugin wires producer's jvmTest and commonTest source directories
 * into the consumer's 'test' source set.
 *
 * Approach: ProjectBuilder (not GradleRunner/TestKit).
 * GradleRunner was ruled out because classloader isolation in TestKit can cause pitest
 * extension resolution issues when the plugin is on the classpath at test time
 * (same issue observed in Task 6). SourceSetContainer IS available via kotlin.jvm apply,
 * but ProjectBuilder gives us reliable, fast, in-process evaluation that is consistent
 * with ToolchainTest and PitestDefaultsTest.
 */
class SourceSetTest {

    @Test
    fun `test source-set includes producer's jvmTest and commonTest`() {
        val project = ProjectBuilder.builder().build() as ProjectInternal
        project.plugins.apply("linc-pitest")
        val ext = project.extensions.getByType(LincPitestExtension::class.java)
        ext.producerModule.set(":producer")
        ext.rootPackage.set("com.fake")
        project.evaluate()

        val testSrcDirs = project.extensions.getByType(SourceSetContainer::class.java)
            .named("test").get().java.srcDirs

        assert(testSrcDirs.any { it.path.contains("producer/src/jvmTest/kotlin") }) {
            "Expected producer's jvmTest/kotlin in srcDirs; got:\n${testSrcDirs.map { it.path }}"
        }
        assert(testSrcDirs.any { it.path.contains("producer/src/commonTest/kotlin") }) {
            "Expected producer's commonTest/kotlin in srcDirs; got:\n${testSrcDirs.map { it.path }}"
        }
    }
}
