package com.linc.quality

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * DSL extension for linc-quality. Lets consumers override defaults per-repo.
 *
 * Example:
 *
 *   lincQuality {
 *       ignoreFailures.set(true)              // soft-gate for legacy repos
 *       detektConfigPath.set("custom.yml")    // override bundled config
 *       excludedPaths.set(listOf("legacy"))   // additional path fragments to skip
 *   }
 *
 * The bundled detekt config (harvested from SpatialLearn + Imagination) is the default.
 * The build and generated directories are always excluded from ktlint regardless of
 * the excludedPaths list.
 */
interface LincQualityExtension {
    /** When true, detekt + ktlint report violations but do NOT fail the build. Default: false. */
    val ignoreFailures: Property<Boolean>

    /** Optional path to a repo-local detekt config (absolute or project-relative). */
    val detektConfigPath: Property<String>

    /** Additional path fragments to exclude from BOTH detekt and ktlint. */
    val excludedPaths: ListProperty<String>
}
