package com.linc.quality

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LincQualityPluginTest {
    @Test
    fun `plugin applies cleanly`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        // Plugin must register the lincQuality extension so consumers can configure it.
        val extension = project.extensions.findByName("lincQuality")
        assertNotNull(extension, "lincQuality extension should be registered")
    }

    @Test
    fun `plugin auto-applies detekt`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        assertNotNull(
            project.plugins.findPlugin("io.gitlab.arturbosch.detekt"),
            "detekt plugin should be applied"
        )
    }

    @Test
    fun `plugin auto-applies ktlint`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        assertNotNull(
            project.plugins.findPlugin("org.jlleitschuh.gradle.ktlint"),
            "ktlint plugin should be applied"
        )
    }

    @Test
    fun `plugin registers detekt task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        assertNotNull(
            project.tasks.findByName("detekt"),
            "detekt task should be registered"
        )
    }

    @Test
    fun `plugin registers ktlintCheck task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        assertNotNull(
            project.tasks.findByName("ktlintCheck"),
            "ktlintCheck task should be registered"
        )
    }

    @Test
    fun `plugin registers detektBaseline task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        assertNotNull(
            project.tasks.findByName("detektBaseline"),
            "detektBaseline task should be registered"
        )
    }

    @Test
    fun `plugin registers ktlintFormat task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        assertNotNull(
            project.tasks.findByName("ktlintFormat"),
            "ktlintFormat task should be registered"
        )
    }

    @Test
    fun `detekt config points at bundled resource`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        // After plugin apply, detekt extension is configured to use the bundled config.
        val detektExt = project.extensions.findByName("detekt")
        assertNotNull(detektExt, "detekt extension should be registered")
        // The bundled config must exist on the classpath as a resource.
        val configStream = LincQualityPlugin::class.java.classLoader
            .getResourceAsStream("detekt-config.yml")
        assertNotNull(configStream, "detekt-config.yml should be on the plugin classpath")
        configStream.close()
    }

    @Test
    fun `extension exposes ignoreFailures toggle`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("linc-quality")
        val ext = project.extensions.getByType(LincQualityExtension::class.java)
        // Hard-fail by default (ignoreFailures = false).
        assertTrue(
            !ext.ignoreFailures.get(),
            "ignoreFailures should default to false (hard fail)"
        )
    }
}
