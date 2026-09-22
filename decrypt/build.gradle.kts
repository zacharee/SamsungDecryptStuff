import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.commons.cli)
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

//    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
tasks.register<Jar>(
    "makeCscChanger"
) //    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
{
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
