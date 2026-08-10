plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.licensee)
    `maven-publish`
    signing
}

group = rootProject.group
version = rootProject.version

kotlin {
    explicitApi()
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
    jvm()
    android {
        namespace = "io.github.yutakax17.advancedhelloworld.compose.messages"
        compileSdk = 37
        minSdk = 24
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.advanced.hello.world.kmp.core)
            api(libs.advanced.hello.world.compose.core)
            api(libs.advanced.hello.world.kmp.messages)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.compose.ui.test)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.compose.desktop.linux)
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/license/mit")
                    distribution.set("repo")
                }
            }
        }
    }
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
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/YutakaX17/compose-advanced-hello-world-messages")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}

signing {
    val signingKey = providers.environmentVariable("SIGNING_KEY").orNull
    val signingPassword = providers.environmentVariable("SIGNING_PASSWORD").orNull
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

licensee {
    allow("Apache-2.0")
    allowUrl("https://opensource.org/license/mit")
}

dependencyAnalysis {
    issues {
        onUnusedDependencies {
            exclude(libs.advanced.hello.world.kmp.core)
            exclude(libs.advanced.hello.world.compose.core)
            exclude(libs.kotlinx.coroutines.core)
            exclude("org.jetbrains.compose.runtime:runtime")
            exclude("org.jetbrains.compose.foundation:foundation")
            exclude("org.jetbrains.compose.material3:material3")
            exclude("org.jetbrains.compose.desktop:desktop-jvm-linux-x64")
            exclude("org.jetbrains.compose.hot-reload:hot-reload-runtime-api")
        }
        onUsedTransitiveDependencies {
            exclude("androidx.compose.foundation:foundation")
            exclude("androidx.compose.foundation:foundation-layout")
            exclude("androidx.compose.material3:material3")
            exclude("androidx.compose.runtime:runtime")
            exclude("androidx.compose.runtime:runtime-desktop")
            exclude("androidx.compose.ui:ui")
            exclude("androidx.compose.ui:ui-desktop")
            exclude("androidx.compose.ui:ui-graphics")
            exclude("androidx.compose.ui:ui-graphics-desktop")
            exclude("androidx.compose.ui:ui-text")
            exclude("androidx.compose.ui:ui-text-desktop")
            exclude("androidx.compose.ui:ui-unit")
            exclude("org.jetbrains.compose.foundation:foundation-desktop")
            exclude("org.jetbrains.compose.foundation:foundation-layout-desktop")
            exclude("org.jetbrains.compose.material3:material3-desktop")
            exclude("org.jetbrains.compose.ui:ui-desktop")
            exclude("org.jetbrains.compose.ui:ui-graphics-desktop")
            exclude("org.jetbrains.compose.ui:ui-text-desktop")
            exclude("org.jetbrains.compose.ui:ui-unit-desktop")
            exclude("org.jetbrains.compose.ui:ui-test-desktop")
            exclude("org.jetbrains.kotlinx:kotlinx-coroutines-test")
            exclude("io.github.yutakax17.advancedhelloworld:core-android")
            exclude("io.github.yutakax17.advancedhelloworld:messages-android")
            exclude("io.github.yutakax17.advancedhelloworld:ui-android")
        }
    }
}
