package metaview

val nodeDefinitions = mapOf(
    "coloredCheckers" to NodeDefinition(
        inputs = mapOf(
            "firstColor" to InputDefinition(
                type = "color"
            ),
            "secondColor" to InputDefinition(
                type = "color"
            )
        ),
        outputType = "bitmap"
    ),
    "checkers" to NodeDefinition(
        inputs = mapOf(),
        outputType = "grayscale"
    ),
    "colorize" to NodeDefinition(
        inputs = mapOf(
            "grayscale" to InputDefinition(
                type = "grayscale"
            ),
            "firstColor" to InputDefinition(
                type = "color"
            ),
            "secondColor" to InputDefinition(
                type = "color"
            )
        ),
        outputType = "bitmap"
    )
)