import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.argparse4j)
    implementation(libs.bcprov)
    implementation(libs.coroutines.core)
    implementation(libs.jssc)
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.create<Jar>("makeCscChanger") {
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
