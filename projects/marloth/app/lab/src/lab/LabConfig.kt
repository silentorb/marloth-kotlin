package lab

import haft.*
import org.lwjgl.glfw.GLFW
import simulation.ViewMode

enum class LabCommandType {
  viewGame,
  viewModel,
  viewWorld,
  viewTexture,
  toggleAbstractView,
  toggleStructureView,

  rotateUp,
  rotateDown,
  rotateLeft,
  rotateRight,

  menu
}

typealias LabInputConfig = MutableMap<String, Bindings<LabCommandType>>

fun createLabInputBindings() = mutableMapOf(
    "global" to createStrokeBindings(0, mapOf(
        GLFW.GLFW_KEY_F1 to LabCommandType.viewGame,
        GLFW.GLFW_KEY_F2 to LabCommandType.viewWorld,
        GLFW.GLFW_KEY_F3 to LabCommandType.viewModel,
        GLFW.GLFW_KEY_F4 to LabCommandType.viewTexture
    ))
        .plus(
            createStrokeBindings(2, mapOf(
                GAMEPAD_BUTTON_START to LabCommandType.menu
            ))
        ),
    "game" to createStrokeBindings<LabCommandType>(0, mapOf(

    )),
    "model" to createBindings(2, mapOf(
        GAMEPAD_AXIS_RIGHT_UP to LabCommandType.rotateUp,
        GAMEPAD_AXIS_RIGHT_DOWN to LabCommandType.rotateDown,
        GAMEPAD_AXIS_RIGHT_LEFT to LabCommandType.rotateLeft,
        GAMEPAD_AXIS_RIGHT_RIGHT to LabCommandType.rotateRight
    )),
    "world" to createStrokeBindings(0, mapOf(
        GLFW.GLFW_KEY_1 to LabCommandType.toggleAbstractView,
        GLFW.GLFW_KEY_2 to LabCommandType.toggleStructureView
    )),
    "texture" to createBindings<LabCommandType>(0, mapOf(

    ))
)

data class WorldViewConfig(
    var showAbstract: Boolean = true,
    var showStructure: Boolean = true
)

data class ModelViewConfig(
    var model: String = "character",
    var drawVertices: Boolean = true,
    var drawEdges: Boolean = true
)

data class LabConfig(
    var view: String = "world",
    var worldView: WorldViewConfig = WorldViewConfig(),
    var modelView: ModelViewConfig = ModelViewConfig()
)

val labInputConfig: LabInputConfig = createLabInputBindings()
