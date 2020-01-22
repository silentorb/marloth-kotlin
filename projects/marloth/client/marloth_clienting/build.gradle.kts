plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

dependencies {
    api("org.lwjgl:lwjgl-glfw:${Versions.lwjgl}")
    implementation("org.lwjgl:lwjgl:${Versions.lwjgl}")
    implementation("org.lwjgl:lwjgl:${Versions.lwjgl}:${Natives.lwjgl}")
    implementation("org.lwjgl:lwjgl-opengl:${Versions.lwjgl}")
    implementation("org.lwjgl:lwjgl-opengl:${Versions.lwjgl}:${Natives.lwjgl}")
}

requires(project, "assets", "definition", "lookinglass", "haft", "marloth_scenery", "spatial", "platforming", "bloom", "bloom_input", "drawing", "simulation", "aura")
