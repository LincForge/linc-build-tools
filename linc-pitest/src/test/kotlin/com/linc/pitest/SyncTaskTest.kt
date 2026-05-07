package com.linc.pitest

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SyncTaskTest {
    @Test
    fun `applying plugin registers syncProducerClasses task`(@TempDir tmp: File) {
        File(tmp, "settings.gradle.kts").writeText(
            """
            rootProject.name = "consumer"
            """.trimIndent()
        )
        File(tmp, "build.gradle.kts").writeText(
            """
            plugins { id("linc-pitest") }
            lincPitest {
                producerModule.set(":fake-producer")
                rootPackage.set("com.fake")
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(tmp)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        assert(result.output.contains("syncProducerClasses")) {
            "Expected syncProducerClasses task; got:\n${result.output}"
        }
    }
}
