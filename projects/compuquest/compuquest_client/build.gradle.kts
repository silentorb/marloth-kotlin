apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

requires(project, "mythic_desktop", "spatial", "platforming", "bloom", "bloom_input", "drawing", "lookinglass", "glowing", "compuquest_simulation")
