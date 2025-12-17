pluginManagement {
    repositories {
        google()           // ✅ 필수!
        mavenCentral()     // ✅ 필수!
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()           // ✅ 필수!
        mavenCentral()
    }
}

rootProject.name = "RemedyAI"
include(":app")