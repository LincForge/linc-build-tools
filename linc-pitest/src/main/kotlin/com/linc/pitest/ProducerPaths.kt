package com.linc.pitest

import org.gradle.api.Project
import java.io.File

internal fun producerClassesDir(project: Project, ext: LincPitestExtension): File = File(
    project.projectDir.parentFile,
    "${ext.producerModule.get().removePrefix(":")}/build/classes/kotlin/jvm/main"
)
