plugins {
    kotlin("jvm")
}

kotlin {
    dependencies {
        implementation("org.bouncycastle:bcprov-jdk15on:1.64")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
        implementation("io.github.java-native:jssc:2.9.4")
        implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    }
}

val fatJar = task("makeCscChanger", type = org.gradle.jvm.tasks.Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("CSCChanger")
    manifest {
        attributes["Implementation-Title"] = "CSC Changer"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "CSCChanger"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
//    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}
