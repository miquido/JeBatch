import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.miquido"
version = "1.0.0-rc"

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.30"

    repositories {
        jcenter()
    }
    
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.3")
    }
}

apply {
    plugin("java")
    plugin("kotlin")
    from("publish-config.gradle")
}

plugins {
    java
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jre8", kotlin_version))
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}