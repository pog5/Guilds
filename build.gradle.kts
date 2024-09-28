plugins {
    kotlin("jvm") version "2.1.0-Beta1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.pog5"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://maven.nostal.ink/repository/maven-snapshots/") {
        name = "leaf-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.aikar.co/content/groups/aikar/") {
        name = "aikar"
    }
}

dependencies {
//    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("cn.dreeam.leaf:leaf-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.2.0")
    implementation("org.mongodb:bson-kotlinx:5.2.0")
}

val targetJavaVersion = 21

kotlin {
    jvmToolchain(targetJavaVersion)

    compilerOptions.javaParameters = true
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}