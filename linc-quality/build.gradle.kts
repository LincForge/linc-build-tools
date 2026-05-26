plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.linc"
version = "0.1.0"

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("lincQuality") {
            id = "linc-quality"
            implementationClass = "com.linc.quality.LincQualityPlugin"
            displayName = "linc-quality convention plugin"
            description = "Detekt + ktlint bundled with LINC defaults for KMP libraries and apps"
        }
    }
}

kotlin {
    jvmToolchain(17)
}
