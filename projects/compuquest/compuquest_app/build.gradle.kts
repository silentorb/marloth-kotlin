apply(from = "${rootProject.projectDir}/build_kotlin.gradle")

requires(project,
        "spatial", "drawing", "bloom", "mythic_desktop",
        "quartz", "platforming", "configuration", "compuquest_client"
)
