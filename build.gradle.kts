import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.10"
  application
  id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

repositories {
  mavenCentral()
}

dependencies {
  api("de.justjanne.libquassel", "libquassel-client", "0.6.1")
  api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.5.0")
  api("org.jetbrains.kotlinx", "kotlinx-cli", "0.3.2")
  api("org.jsoup", "jsoup", "1.13.1")
}

tasks.withType<Jar> {
  manifest {
    attributes["Main-Class"] = "de.justjanne.titlebot.MainKt"
  }
  exclude("META-INF/**")
  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

application {
  mainClass.set("de.justjanne.titlebot.MainKt")
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf(
      "-Xinline-classes",
      "-Xopt-in=kotlin.ExperimentalUnsignedTypes"
    )
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}
