
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://repository.cloudera.com/artifactory/cloudera-repos/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://repository.cloudera.com/artifactory/cloudera-repos/")
    }
}

rootProject.name = "cirrina"

include("core", "runtime")