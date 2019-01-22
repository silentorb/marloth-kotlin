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
const val floatListType = "FloatList"
const val intType = "Int"
const val noneType = "None"

const val textureOutput = "textureOutput"

val connectableTypes = setOf(bitmapType, grayscaleType)

data class InputDefinition(
    val type: String,
    val defaultValue: Any? = null
)

data class NodeDefinition(
    val inputs: Map<String, InputDefinition>,
    val outputType: String,
    val variableInputs: String? = null
)

typealias NodeDefinitionMap = Map<String, NodeDefinition>

val nodeDefinitions: NodeDefinitionMap = mapOf(
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
            "mixer" to InputDefinition(
                type = floatListType,
                defaultValue = listOf<Float>()
            )
        ),
        variableInputs = bitmapType,
        outputType = bitmapType
    ),
    "mixGrayscales" to NodeDefinition(
        inputs = mapOf(
            "weights" to InputDefinition(
                type = floatListType,
                defaultValue = listOf<Float>()
            )
        ),
        variableInputs = grayscaleType,
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
