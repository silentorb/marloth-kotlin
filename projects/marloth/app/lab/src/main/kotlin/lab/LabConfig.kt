package lab

import haft.*
import lab.views.TextureViewConfig
import lab.views.game.GameViewConfig
import lab.views.map.MapViewConfig
import lab.views.map.mapViewBindings
import lab.views.model.ModelViewConfig
import org.lwjgl.glfw.GLFW

enum class LabCommandType {
  viewGame,
  viewMap,
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

  moveUp,
  moveDown,
  moveLeft,
  moveRight,

  menu,
  update,

  select,
  zoomIn,
  zoomOut,
  incrementRaySkip,
  decrementRaySkip,

  cameraViewFront,
  cameraViewBack,
  cameraViewTop,
  cameraViewBottom,
  cameraViewRight,
  cameraViewLeft,

  rebuildWorld,
  selectModeEdges,
  selectModeFaces,
  selectModeVertices,
  toggleSelection,

  toggleMeshDisplay,
  toggleNormals,
  toggleFaceIds,
  toggleAbstract,
  toggleNodeIds,
  toggleDrawPhysics,
  toggleIsolateSelection,
  toggleWireframe,

  switchCamera,

  selectEdgeLoop
}

val labCommandStrokes = setOf(
    LabCommandType.viewGame,
    LabCommandType.viewMap,
    LabCommandType.viewModel,
    LabCommandType.viewWorld,
    LabCommandType.viewTexture,
    LabCommandType.toggleAbstractView,
    LabCommandType.toggleStructureView,
    LabCommandType.menu,
    LabCommandType.update,
    LabCommandType.select,
    LabCommandType.incrementRaySkip,
    LabCommandType.decrementRaySkip,

    LabCommandType.cameraViewFront,
    LabCommandType.cameraViewBack,
    LabCommandType.cameraViewTop,
    LabCommandType.cameraViewBottom,
    LabCommandType.cameraViewRight,
    LabCommandType.cameraViewLeft,

    LabCommandType.selectModeEdges,
    LabCommandType.selectModeFaces,
    LabCommandType.selectModeVertices,
    LabCommandType.toggleSelection,

    LabCommandType.toggleMeshDisplay,
    LabCommandType.toggleNormals,
    LabCommandType.toggleFaceIds,
    LabCommandType.toggleIsolateSelection,
    LabCommandType.toggleNodeIds,
    LabCommandType.toggleDrawPhysics,
    LabCommandType.toggleAbstract,
    LabCommandType.toggleWireframe,

    LabCommandType.selectEdgeLoop
)

typealias LabInputConfig = MutableMap<Views, Bindings<LabCommandType>>

fun createLabInputBindings() = mutableMapOf(
    Views.global to createBindings(DeviceIndex.keyboard, mapOf(
        GLFW.GLFW_KEY_F1 to LabCommandType.viewGame,
        GLFW.GLFW_KEY_F2 to LabCommandType.viewMap,
        GLFW.GLFW_KEY_F3 to LabCommandType.viewWorld,
        GLFW.GLFW_KEY_F4 to LabCommandType.viewModel,
        GLFW.GLFW_KEY_F5 to LabCommandType.viewTexture,
        GLFW.GLFW_KEY_P to LabCommandType.toggleDrawPhysics

    ))
        .plus(
            createBindings(DeviceIndex.gamepad, mapOf(
                GAMEPAD_BUTTON_START to LabCommandType.menu
            ))
        ),
    Views.game to createBindings<LabCommandType>(DeviceIndex.keyboard, mapOf(

    ))
        .plus(createBindings(DeviceIndex.keyboard, mapOf(
            GLFW.GLFW_KEY_X to LabCommandType.toggleMeshDisplay
        ))),
    Views.model to createBindings(DeviceIndex.gamepad, mapOf(
        GAMEPAD_AXIS_RIGHT_UP to LabCommandType.rotateUp,
        GAMEPAD_AXIS_RIGHT_DOWN to LabCommandType.rotateDown,
        GAMEPAD_AXIS_RIGHT_LEFT to LabCommandType.rotateLeft,
        GAMEPAD_AXIS_RIGHT_RIGHT to LabCommandType.rotateRight
    )).plus(createBindings(DeviceIndex.gamepad, mapOf(
        GAMEPAD_BUTTON_Y to LabCommandType.update
    )))
        .plus(createBindings(DeviceIndex.keyboard, mapOf(
            GLFW.GLFW_KEY_KP_1 to LabCommandType.cameraViewFront,
            GLFW.GLFW_KEY_KP_3 to LabCommandType.cameraViewBack,
            GLFW.GLFW_KEY_KP_4 to LabCommandType.cameraViewLeft,
            GLFW.GLFW_KEY_KP_6 to LabCommandType.cameraViewRight,
            GLFW.GLFW_KEY_KP_7 to LabCommandType.cameraViewTop,
            GLFW.GLFW_KEY_KP_9 to LabCommandType.cameraViewBottom,

            GLFW.GLFW_KEY_L to LabCommandType.selectEdgeLoop,
            GLFW.GLFW_KEY_X to LabCommandType.toggleMeshDisplay,
            GLFW.GLFW_KEY_N to LabCommandType.toggleNormals,

            GLFW.GLFW_KEY_A to LabCommandType.toggleSelection,
            GLFW.GLFW_KEY_E to LabCommandType.selectModeEdges,
            GLFW.GLFW_KEY_F to LabCommandType.selectModeFaces,
            GLFW.GLFW_KEY_V to LabCommandType.selectModeVertices
        )))
        .plus(createBindings(DeviceIndex.keyboard, mapOf(
            GLFW.GLFW_KEY_W to LabCommandType.pan,
            GLFW.GLFW_KEY_LEFT to LabCommandType.rotateLeft,
            GLFW.GLFW_KEY_RIGHT to LabCommandType.rotateRight

        )))
        .plus(createBindings(DeviceIndex.mouse, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_2 to LabCommandType.rotate,
            MOUSE_SCROLL_UP to LabCommandType.zoomIn,
            MOUSE_SCROLL_DOWN to LabCommandType.zoomOut
        )))
        .plus(createBindings(DeviceIndex.mouse, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_1 to LabCommandType.select
        )))
        .plus(createBindings(DeviceIndex.mouse, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_1 to LabCommandType.select
        ))),
    Views.map to mapViewBindings()
)

data class WorldViewConfig(
    var showAbstract: Boolean = true,
    var showStructure: Boolean = true
)

enum class Views {
  game,
  global,
  map,
  model
}

data class LabConfig(
    var view: Views = Views.game,
    var modelView: ModelViewConfig = ModelViewConfig(),
    var gameView: GameViewConfig = GameViewConfig(),
    var mapView: MapViewConfig = MapViewConfig()
)

val labInputConfig: LabInputConfig = createLabInputBindings()
