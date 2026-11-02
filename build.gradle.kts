plugins {
    id("com.android.application") version "9.1.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
}

val recentDockBuildKey = Integer.toHexString(rootProject.projectDir.absolutePath.lowercase().hashCode())
val recentDockBuildRoot = file("${System.getProperty("user.home")}/.gradle/recentdock-build/$recentDockBuildKey")
layout.buildDirectory.set(recentDockBuildRoot.resolve("root"))
subprojects {
    layout.buildDirectory.set(recentDockBuildRoot.resolve(project.name))
}
