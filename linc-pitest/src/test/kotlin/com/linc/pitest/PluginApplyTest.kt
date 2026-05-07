package com.linc.pitest

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class PluginApplyTest {
    @Test
    fun `plugin applies and registers lincPitest extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-pitest")
        val extension = project.extensions.findByName("lincPitest")
        assertNotNull(extension, "lincPitest extension should be registered")
    }

    @Test
    fun `plugin auto-applies kotlin-jvm and pitest plugins`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-pitest")
        assertNotNull(project.plugins.findPlugin("org.jetbrains.kotlin.jvm"),
            "kotlin-jvm should be applied")
        assertNotNull(project.plugins.findPlugin("info.solidsoft.pitest"),
            "info.solidsoft.pitest should be applied")
    }
}
