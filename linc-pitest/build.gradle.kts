plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
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
        create("lincPitest") {
            id = "linc-pitest"
            implementationClass = "com.linc.pitest.LincPitestPlugin"
            displayName = "linc-pitest convention plugin"
            description = "Pitest configuration for KMP libraries via consumer-module pattern"
        }
    }
}

kotlin {
    jvmToolchain(17)
}
