plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("com.github.node-gradle.node") version "7.0.2"
}

group = "com.qingyingliu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2025.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        bundledPlugin("org.jetbrains.kotlin")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }
        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    named("instrumentCode") {
        enabled = false
    }
    named("instrumentTestCode") {
        enabled = false
    }
    withType<Test> {
        ignoreFailures = true
    }
}

// Node.js plugin configuration
node {
    version.set("20.11.0")
    download.set(true)
    npmCommand.set("npm")
}

// Frontend build tasks
val buildFrontend by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    group = "build"
    description = "Install frontend dependencies"
    workingDir.set(file("frontend"))
    args.set(listOf("install"))
}

val compileFrontend by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    group = "build"
    description = "Build frontend assets"
    workingDir.set(file("frontend"))
    dependsOn(buildFrontend)
    args.set(listOf("run", "build"))
}

//// Make sure backend build depends on frontend build
//tasks.named("runIde") {
//    dependsOn(compileFrontend)
//}
