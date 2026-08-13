plugins {
    kotlin("jvm") version "2.4.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
    id("org.jetbrains.compose") version "1.11.1"
}

dependencies {
    implementation(
        "io.github.yutakax17.advancedhelloworld:" +
            "compose-advanced-hello-world-messages-jvm:0.2.0-SNAPSHOT",
    )
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.compose.ui:ui-test:1.11.1")
    testImplementation("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:1.11.1")
}

tasks.test {
    useJUnitPlatform()
}
