pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Репозиторий Yandex
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
    }
}

rootProject.name = "Cloova"
include(":app")
 