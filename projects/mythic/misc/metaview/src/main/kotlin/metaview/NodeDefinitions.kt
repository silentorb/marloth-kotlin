package metaview

data class InputDefinition(
    val type: String
)

data class NodeDefinition(
    val inputs: Map<String, InputDefinition>,
    val outputType: String
)

val nodeDefinitions = mapOf(
    "checkers" to NodeDefinition(
        inputs = mapOf(
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