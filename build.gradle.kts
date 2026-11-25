plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.19.3"
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

base {
    archivesName.set(providers.gradleProperty("archives_base_name"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.fabricmc.net/") }
    // ModMenu is published here, not on Maven Central or Fabric Maven.
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    // Reconstructed from bytecode: Minecraft members keep their intermediary names
    // (class_XXX / method_XXX / field_XXX). Compile against intermediary mappings
    // so those references resolve without renaming any source.
    mappings("net.fabricmc:intermediary:${property("minecraft_version")}:v2")

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    // ModMenu is optional; compile against it so the modmenu entrypoint resolves.
    modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

loom {
    // The original JAR was built with Fabric-Loom-Mixin-Remap-Type: static.
    // Defaults are appropriate.
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<Jar> {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
