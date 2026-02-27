plugins {
    java
    application
}

group = "org.minecraftclone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.4.1"
val jomlVersion  = "1.10.7"

val os = System.getProperty("os.name").lowercase()
val arch = System.getProperty("os.arch").lowercase()

val lwjglNatives = when {
    os.contains("win") ->
        if (arch.contains("aarch64")) "natives-windows-arm64" else "natives-windows"
    os.contains("mac") ->
        if (arch.contains("aarch64")) "natives-macos-arm64" else "natives-macos"
    else ->
        if (arch.contains("aarch64")) "natives-linux-arm64" else "natives-linux"
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")

    implementation("org.joml:joml:$jomlVersion")

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
}

application {
    mainClass.set("org.minecraftclone.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}