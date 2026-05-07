package com.linc.pitest

import org.gradle.api.JavaVersion
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ToolchainTest {
    @Test
    fun `default toolchain is 21`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-pitest")
        val lincExt = project.extensions.getByType(LincPitestExtension::class.java)
        lincExt.producerModule.set(":fake")
        lincExt.rootPackage.set("com.fake")
        // Force afterEvaluate
        (project as org.gradle.api.internal.project.ProjectInternal).evaluate()
        val ext = project.extensions.getByType(
            org.gradle.api.plugins.JavaPluginExtension::class.java
        )
        assertEquals(JavaVersion.VERSION_21, ext.targetCompatibility)
    }

    @Test
    fun `explicit toolchain override is honored`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-pitest")
        val lincExt = project.extensions.getByType(LincPitestExtension::class.java)
        lincExt.producerModule.set(":fake")
        lincExt.rootPackage.set("com.fake")
        lincExt.jvmToolchain.set(17)
        (project as org.gradle.api.internal.project.ProjectInternal).evaluate()
        val ext = project.extensions.getByType(
            org.gradle.api.plugins.JavaPluginExtension::class.java
        )
        assertEquals(JavaVersion.VERSION_17, ext.targetCompatibility)
    }
}
