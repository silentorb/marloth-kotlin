apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

requires(project,
    "spatial", "drawing", "bloom", "generation_architecture", "mythic_desktop",
    "marloth_clienting", "quartz", "platforming", "marloth_game",
    "simulation", "configuration"
)
