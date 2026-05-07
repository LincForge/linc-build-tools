package com.linc.pitest

import org.gradle.api.Project
import org.gradle.api.tasks.Sync

internal fun configureProducerSync(project: Project, ext: LincPitestExtension) {
    val producerClassesDir = producerClassesDir(project, ext)

    val syncTask = project.tasks.register("syncProducerClasses", Sync::class.java,
        org.gradle.api.Action {
            from(producerClassesDir)
            into(project.layout.buildDirectory.dir("classes/kotlin/main"))
            dependsOn("${ext.producerModule.get()}:compileKotlinJvm")
        }
    )

    project.tasks.named("classes",
        org.gradle.api.Action { dependsOn(syncTask) })
    project.tasks.named("pitest",
        org.gradle.api.Action { dependsOn(syncTask) })
}
