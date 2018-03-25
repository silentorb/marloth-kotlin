package lab

import haft.*
import org.lwjgl.glfw.GLFW
import lab.views.model.ModelViewConfig

enum class LabCommandType {
  viewGame,
  viewModel,
  viewWorld,
  viewTexture,
  toggleAbstractView,
  toggleStructureView,

  pan,
  rotate,
  rotateUp,
  rotateDown,
  rotateLeft,
  rotateRight,

  menu,
  update,

  select,
  zoomIn,
  zoomOut,

  cameraViewFront,
  cameraViewBack,
  cameraViewTop,
  cameraViewBottom,
  cameraViewRight,
  cameraViewLeft,

  selectModeEdges,
  selectModeFaces,
  selectModeVertices,
  toggleSelection,

  toggleMeshDisplay,

  selectEdgeLoop
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
    )).plus(createStrokeBindings(2, mapOf(
        GAMEPAD_BUTTON_Y to LabCommandType.update
    )))
        .plus(createStrokeBindings(0, mapOf(
            GLFW.GLFW_KEY_KP_1 to LabCommandType.cameraViewFront,
            GLFW.GLFW_KEY_KP_3 to LabCommandType.cameraViewBack,
            GLFW.GLFW_KEY_KP_4 to LabCommandType.cameraViewLeft,
            GLFW.GLFW_KEY_KP_6 to LabCommandType.cameraViewRight,
            GLFW.GLFW_KEY_KP_7 to LabCommandType.cameraViewTop,
            GLFW.GLFW_KEY_KP_9 to LabCommandType.cameraViewBottom,

            GLFW.GLFW_KEY_L to LabCommandType.selectEdgeLoop,
            GLFW.GLFW_KEY_X to LabCommandType.toggleMeshDisplay,

            GLFW.GLFW_KEY_A to LabCommandType.toggleSelection,
            GLFW.GLFW_KEY_E to LabCommandType.selectModeEdges,
            GLFW.GLFW_KEY_F to LabCommandType.selectModeFaces,
            GLFW.GLFW_KEY_V to LabCommandType.selectModeVertices
        )))
        .plus(createBindings(0, mapOf(
            GLFW.GLFW_KEY_W to LabCommandType.pan
        )))
        .plus(createBindings(1, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_2 to LabCommandType.rotate,
            MOUSE_SCROLL_UP to LabCommandType.zoomIn,
            MOUSE_SCROLL_DOWN to LabCommandType.zoomOut
        )))
        .plus(createStrokeBindings(1, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_1 to LabCommandType.select
        ))),
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

data class LabConfig(
    var view: String = "world",
    var worldView: WorldViewConfig = WorldViewConfig(),
    var modelView: ModelViewConfig = ModelViewConfig(),
    var gameView: lab.views.GameViewConfig = lab.views.GameViewConfig()
)

val labInputConfig: LabInputConfig = createLabInputBindings()
