pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

includeBuild("../kmp-advanced-hello-world-core") {
    dependencySubstitution {
        substitute(module("io.github.yutakax17.advancedhelloworld:kmp-advanced-hello-world-core"))
            .using(project(":core"))
    }
}
includeBuild("../compose-advanced-hello-world-core") {
    dependencySubstitution {
        substitute(module("io.github.yutakax17.advancedhelloworld:compose-advanced-hello-world-core"))
            .using(project(":ui"))
    }
}
includeBuild("../kmp-advanced-hello-world-messages") {
    dependencySubstitution {
        substitute(module("io.github.yutakax17.advancedhelloworld:kmp-advanced-hello-world-messages"))
            .using(project(":messages"))
    }
}

rootProject.name = "compose-advanced-hello-world-messages"
include(":messages-ui")
