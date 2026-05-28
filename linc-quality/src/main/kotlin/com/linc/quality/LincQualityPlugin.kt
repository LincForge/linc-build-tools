package com.linc.quality

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.io.File

/**
 * linc-quality convention plugin.
 *
 * Bundles detekt + ktlint with LINC defaults so consumers get `./gradlew detekt` and
 * `./gradlew ktlintCheck` (hard-fail by default) by applying one plugin id.
 *
 * The bundled detekt config lives at `src/main/resources/detekt-config.yml` and is
 * extracted to `<project>/build/linc-quality/detekt-config.yml` at apply-time so that
 * detekt-gradle-plugin can resolve it as a regular file.
 */
class LincQualityPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("lincQuality", LincQualityExtension::class.java)
        ext.ignoreFailures.convention(false)
        ext.excludedPaths.convention(emptyList())

        project.plugins.apply("io.gitlab.arturbosch.detekt")
        project.plugins.apply("org.jlleitschuh.gradle.ktlint")

        configureDetekt(project, ext)
        configureKtlint(project, ext)
        registerBaselineTasks(project)
    }

    private fun configureDetekt(project: Project, ext: LincQualityExtension) {
        val detektExt = project.extensions.getByType(DetektExtension::class.java)

        // Extract bundled config to build dir so detekt can read it as a File.
        val configFile = extractBundledDetektConfig(project)

        detektExt.buildUponDefaultConfig = true
        detektExt.allRules = false
        detektExt.parallel = true

        // Pin machine-readable reports so the advisory-signal lint pillar parser
        // (linc-ops nightly-lint) finds the same files on every consumer:
        //   XML   → <module>/build/reports/detekt/detekt.xml (checkstyle format, parsed for counts)
        //   SARIF → <module>/build/reports/detekt/detekt.sarif (free; deferred code-scanning follow-on)
        project.tasks.withType(Detekt::class.java).configureEach {
            reports.xml.required.set(true)
            reports.sarif.required.set(true)
        }

        // KMP source-set discovery: detekt-gradle-plugin's default source set search only
        // picks up src/main/kotlin (JVM convention). KMP repos use src/commonMain/kotlin,
        // src/androidMain/kotlin, etc. Add common KMP source dirs so the detekt task has
        // something to lint.
        val kmpDirs = listOf(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/jvmMain/kotlin",
            "src/iosMain/kotlin",
            "src/watchosMain/kotlin",
            "src/wasmJsMain/kotlin",
        ).map { project.file(it) }.filter { it.exists() }
        if (kmpDirs.isNotEmpty()) {
            detektExt.source.setFrom(kmpDirs)
        }

        // Consumer can override via lincQuality { detektConfigPath = "..." }.
        project.afterEvaluate {
            val overridePath = ext.detektConfigPath.orNull
            val effectiveConfig = if (overridePath != null) {
                project.file(overridePath)
            } else {
                configFile
            }
            detektExt.config.setFrom(project.files(effectiveConfig))
            detektExt.ignoreFailures = ext.ignoreFailures.get()
        }
    }

    private fun configureKtlint(project: Project, ext: LincQualityExtension) {
        val ktlintExt = project.extensions.getByType(KtlintExtension::class.java)

        ktlintExt.android.set(false)
        ktlintExt.outputColorName.set("RED")
        ktlintExt.coloredOutput.set(true)
        ktlintExt.verbose.set(false)

        // Emit a checkstyle-format XML report so the advisory-signal lint pillar parser
        // can count ktlint violations the same way it counts detekt's. ktlint-gradle writes
        // one report per source-set check task under build/reports/ktlint/<task>/<task>.xml.
        ktlintExt.reporters {
            reporter(ReporterType.CHECKSTYLE)
        }

        project.afterEvaluate {
            ktlintExt.ignoreFailures.set(ext.ignoreFailures.get())
        }

        // Exclude generated + build dirs from ktlint. Note: ktlint-gradle's filter block
        // is a PatternFilterable receiver; use the standard exclude() globs.
        ktlintExt.filter {
            exclude("**/build/**")
            exclude("**/generated/**")
        }
    }

    /**
     * `detektBaseline` and `ktlintFormat` are first-party tasks of the detekt/ktlint plugins.
     * We don't need to register them — applying those plugins already does. We just verify
     * presence by re-looking-up after apply so failure modes are explicit.
     */
    private fun registerBaselineTasks(project: Project) {
        project.afterEvaluate {
            // No-op: tasks are provided by the underlying plugins. Listed here for documentation
            // of the contract surfaced in LincQualityPluginTest.
            requireNotNull(project.tasks.findByName("detektBaseline")) {
                "detekt plugin should register detektBaseline task"
            }
            requireNotNull(project.tasks.findByName("ktlintFormat")) {
                "ktlint plugin should register ktlintFormat task"
            }
        }
    }

    private fun extractBundledDetektConfig(project: Project): File {
        val outDir = project.layout.buildDirectory.dir("linc-quality").get().asFile
        outDir.mkdirs()
        val outFile = File(outDir, "detekt-config.yml")
        if (!outFile.exists()) {
            val stream = LincQualityPlugin::class.java.classLoader
                .getResourceAsStream("detekt-config.yml")
                ?: error("Bundled detekt-config.yml not found on plugin classpath")
            stream.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return outFile
    }
}
