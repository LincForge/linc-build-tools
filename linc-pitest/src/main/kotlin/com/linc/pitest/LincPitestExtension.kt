package com.linc.pitest

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class LincPitestExtension {
    /** Path to the producer KMP module, e.g. ":sensorkit". */
    abstract val producerModule: Property<String>

    /** Root package of the producer's commonMain code, e.g. "com.lincmobile.sensorkit". */
    abstract val rootPackage: Property<String>

    /** Test files (glob patterns) to exclude from compileTestKotlin —
     *  workaround for cross-module `internal` access (gotcha e2c5cd3a). */
    abstract val excludedTestFiles: ListProperty<String>

    /** Producer JVM toolchain version. Defaults to 21 (matches LINC fleet). */
    abstract val jvmToolchain: Property<Int>
}
