plugins {
    `java-library`
    id("base.base-conventions")
    id("base.fill-build-constants")
    id("viabedrock.publishing-conventions")
    id("via.run-with-viaproxy-task")
    id("net.raphimc.class-token-replacer") version "1.1.7"
}

val tool by sourceSets.creating {
    compileClasspath += sourceSets["main"].output + sourceSets["main"].compileClasspath
    runtimeClasspath += output + compileClasspath
}

repositories {
    maven {
        name = "ViaVersion"
        url = uri("https://repo.viaversion.com")
        content {
            includeGroupByRegex("com\\.viaversion(\\..+)?")
            includeGroupByRegex("net\\.raphimc(\\..+)?")
        }
    }
    maven {
        name = "Minecraft Libraries"
        url = uri("https://libraries.minecraft.net")
        content {
            includeGroup("com.mojang")
        }
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
        content {
            includeGroupByRegex("com\\.github\\..+")
        }
    }
    maven {
        name = "OpenCollab"
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }
    mavenLocal()
}

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.12.0")
    compileOnly("org.yaml:snakeyaml:2.7")
    compileOnly("com.google.guava:guava:33.7.1-jre")
    compileOnly("io.netty:netty-handler:4.2.18.Final")
    compileOnly("com.google.code.gson:gson:2.14.0")
    compileOnly("org.cloudburstmc.netty:netty-transport-raknet:2.0.0.CR3-SNAPSHOT")
    compileOnly("org.cloudburstmc.netty:netty-transport-nethernet:2.0.0.CR3-SNAPSHOT")

    api("io.netty:netty-codec-http:4.2.18.Final") {
        isTransitive = false
    }
    api("io.netty:netty-codec-compression:4.2.18.Final") {
        isTransitive = false
    }
    api("io.jsonwebtoken:jjwt-impl:0.13.0")
    api("io.jsonwebtoken:jjwt-gson:0.13.0") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    api("net.lenni0451.mcstructs-bedrock:text:2.0.1") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    api("net.lenni0451.mcstructs-bedrock:forms:2.0.1") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    api("com.viaversion.mcstructs:dialog:5-3.2.2-20251210.004302-1") {
        exclude(group = "com.viaversion.mcstructs", module = "core")
        exclude(group = "com.viaversion.mcstructs", module = "text")
        exclude(group = "com.viaversion.mcstructs", module = "snbt")
        exclude(group = "com.viaversion.mcstructs", module = "converter")
    }
    api("com.vdurmont:semver4j:3.1.0")
    api("com.mojang:brigadier:1.3.10")
    api("at.yawk.lz4:lz4-java:1.11.2")
    api("com.github.oryxel1:CubeConverter:abecffaaea") {
        isTransitive = false
    }
    api("team.unnamed:mocha:3.0.1") {
        isTransitive = false
    }

    add("toolImplementation", "org.jsoup:jsoup:1.23.2")
    add("toolImplementation", "org.ow2.asm:asm-tree:9.10.1")
    add("toolImplementation", "net.lenni0451.commons:asm:1.9.2")
}
