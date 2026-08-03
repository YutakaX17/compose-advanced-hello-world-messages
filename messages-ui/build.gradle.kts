plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

kotlin {
    explicitApi()
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.advanced.hello.world.kmp.core)
            api(libs.advanced.hello.world.compose.core)
            api(libs.advanced.hello.world.kmp.messages)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

publishing {
    publications.named<MavenPublication>("kotlinMultiplatform") {
        artifactId = "compose-advanced-hello-world-messages"
    }
    publications.named<MavenPublication>("jvm") {
        artifactId = "compose-advanced-hello-world-messages-jvm"
    }
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}
