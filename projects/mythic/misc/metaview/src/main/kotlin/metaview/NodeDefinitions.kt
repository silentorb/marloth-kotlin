package metaview

import mythic.spatial.Vector3

data class InputDefinition(
    val type: String,
    val defaultValue: Any?
)

data class NodeDefinition(
    val inputs: Map<String, InputDefinition>,
    val outputType: String
)

val nodeDefinitions: Map<String, NodeDefinition> = mapOf(
    "coloredCheckers" to NodeDefinition(
        inputs = mapOf(
            "firstColor" to InputDefinition(
                type = "color",
                defaultValue = Vector3(0f)
            ),
            "secondColor" to InputDefinition(
                type = "color",
                defaultValue = Vector3(1f)
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
                type = "grayscale",
                defaultValue = null
            ),
            "firstColor" to InputDefinition(
                type = "color",
                defaultValue = Vector3(0f)
            ),
            "secondColor" to InputDefinition(
                type = "color",
                defaultValue = Vector3(1f)
            )
        ),
        outputType = "bitmap"
    )
)