plugins {
    kotlin("jvm")
}

kotlin {
    dependencies {
        implementation("org.bouncycastle:bcprov-jdk15on:1.64")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
        implementation("io.github.java-native:jssc:2.9.4")
    }
}
