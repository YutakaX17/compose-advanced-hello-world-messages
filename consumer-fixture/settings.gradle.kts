pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("../messages-ui/build/repo") }
        providers.gradleProperty("localMavenRepository").orNull?.let {
            maven(url = uri(it))
        }
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "compose-messages-consumer-fixture"
