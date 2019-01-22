package metaview

import mythic.spatial.Vector3

data class FloatRange(
    val min: Float,
    val max: Float
)

const val bitmapType = "Bitmap"
const val colorType = "Color"
const val grayscaleType = "Grayscale"
const val floatType = "Float"
const val intType = "Int"
const val noneType = "None"

const val textureOutput = "textureOutput"

data class InputDefinition(
    val type: String,
    val defaultValue: Any? = null
)

data class NodeDefinition(
    val inputs: Map<String, InputDefinition>,
    val outputType: String
)

typealias NodeDefinitionMap = Map<String, NodeDefinition>

val nodeDefinitions: Map<String, NodeDefinition> = mapOf(
    "coloredCheckers" to NodeDefinition(
        inputs = mapOf(
            "firstColor" to InputDefinition(
                type = colorType,
                defaultValue = Vector3(0f)
            ),
            "secondColor" to InputDefinition(
                type = colorType,
                defaultValue = Vector3(1f)
            )
        ),
        outputType = bitmapType
    ),
    "checkers" to NodeDefinition(
        inputs = mapOf(),
        outputType = grayscaleType
    ),
    "colorize" to NodeDefinition(
        inputs = mapOf(
            "grayscale" to InputDefinition(
                type = grayscaleType
            ),
            "firstColor" to InputDefinition(
                type = colorType,
                defaultValue = Vector3(0f)
            ),
            "secondColor" to InputDefinition(
                type = colorType,
                defaultValue = Vector3(1f)
            )
        ),
        outputType = bitmapType
    ),
    "mixBitmaps" to NodeDefinition(
        inputs = mapOf(
            "first" to InputDefinition(
                type = bitmapType
            ),
            "second" to InputDefinition(
                type = bitmapType
            ),
            "degree" to InputDefinition(
                type = floatType
            )
        ),
        outputType = bitmapType
    ),
    "mixGrayscales" to NodeDefinition(
        inputs = mapOf(
            "first" to InputDefinition(
                type = grayscaleType
            ),
            "second" to InputDefinition(
                type = grayscaleType
            ),
            "degree" to InputDefinition(
                type = floatType,
                defaultValue = 0.5f
            )
        ),
        outputType = grayscaleType
    ),
    "perlinNoise" to NodeDefinition(
        inputs = mapOf(
            "periods" to InputDefinition(
                type = intType,
                defaultValue = 0.5f
            )
        ),
        outputType = grayscaleType
    ),
    textureOutput to NodeDefinition(
        inputs = mapOf(
            "diffuse" to InputDefinition(
                type = bitmapType
            )
        ),
        outputType = noneType
    )
)
