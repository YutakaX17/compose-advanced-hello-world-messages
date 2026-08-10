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
        providers.gradleProperty("localMavenRepository").orNull?.let {
            maven(url = uri(it))
        }
        mavenCentral()
        google()
        mavenLocal()
        listOf(
            "kmp-advanced-hello-world-core",
            "compose-advanced-hello-world-core",
            "kmp-advanced-hello-world-messages",
        ).forEach { repositoryName ->
            maven {
                name = "GitHubPackages${repositoryName.replace("-", "")}"
                url = uri("https://maven.pkg.github.com/YutakaX17/$repositoryName")
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").orNull
                    password = providers.environmentVariable("GITHUB_TOKEN").orNull
                }
            }
        }
    }
}

if (providers.gradleProperty("useLocalFamilyBuilds").orNull == "true") {
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
}

rootProject.name = "compose-advanced-hello-world-messages"
include(":messages-ui")
