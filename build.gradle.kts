import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.9.28"
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
    }
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("http4k-cloudnative-graalvm")
            mainClass.set("com.example.Http4kCloudNativeKt")
            useFatJar.set(true)
        }
    }
}

val http4kVersion: String by project
val http4kConnectVersion: String by project
val junitVersion: String by project
val kotlinVersion: String by project
val javaVersion: String by properties

application {
    mainClass = "com.example.Http4kCloudNativeKt"
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier = null
        archiveVersion = null
        mergeServiceFiles()
        dependsOn(distTar, distZip)
        isZip64 = true
    }
}

repositories {
    mavenCentral()
}

apply(plugin = "kotlin")

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = false
            jvmTarget = "21"
            freeCompilerArgs += "-Xjvm-default=all"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

dependencies {
    implementation("org.http4k:http4k-client-fuel:${http4kVersion}")
    implementation("org.http4k:http4k-cloudnative:${http4kVersion}")
    implementation("org.http4k:http4k-connect-storage-http:${http4kConnectVersion}")
    implementation("org.http4k:http4k-connect-storage-jdbc:${http4kConnectVersion}")
    implementation("org.http4k:http4k-connect-storage-redis:${http4kConnectVersion}")
    implementation("org.http4k:http4k-connect-storage-s3:${http4kConnectVersion}")
    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.http4k:http4k-format-gson:${http4kVersion}")
    implementation("org.http4k:http4k-format-jackson-yaml:${http4kVersion}")
    implementation("org.http4k:http4k-format-moshi:${http4kVersion}")
    implementation("org.http4k:http4k-metrics-micrometer:${http4kVersion}")
    implementation("org.http4k:http4k-opentelemetry:${http4kVersion}")
    //implementation("org.http4k:http4k-server-ratpack:${http4kVersion}")
    implementation("org.http4k:http4k-template-jte:${http4kVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    testImplementation("org.http4k:http4k-testing-approval:${http4kVersion}")
    testImplementation("org.http4k:http4k-testing-hamkrest:${http4kVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

