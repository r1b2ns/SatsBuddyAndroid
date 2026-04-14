pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SatsBuddy"
include(":app")

include(":cktap-android")
project(":cktap-android").projectDir = file("../rust-cktap/cktap-android/lib")
