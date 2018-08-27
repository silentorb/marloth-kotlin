package lab

import haft.*
import lab.views.*
import lab.views.game.GameViewRenderData
import lab.views.game.renderScene
import lab.views.game.updateGame
import lab.views.game.updateGameViewState
import lab.views.map.createTopDownCamera
import lab.views.map.renderMapView
import lab.views.map.updateMapState
import lab.views.model.ModelView
import lab.views.model.ModelViewState
import lab.views.world.WorldView
import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.CommandType
import marloth.clienting.InputState
import marloth.clienting.gui.MenuActionType
import marloth.clienting.gui.menuButtonAction
import marloth.clienting.gui.updateMenuState
import mythic.bloom.Box
import mythic.bloom.renderLayout
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import org.joml.minus
import rendering.createCanvas
import scenery.Screen
import simulation.AbstractWorld
import simulation.World
import visualizing.createScenes

data class LabState(
    val labInput: InputTriggerState<LabCommandType>,
    val gameInput: ProfileStates<CommandType>,
    val modelViewState: ModelViewState,
    val gameClientState: ClientState
)

fun createLabDeviceHandlers(input: PlatformInput): List<ScalarInputSource> {
  val gamepad = input.getGamepads().firstOrNull()
  return listOf(
      input.KeyboardInputSource,
      input.MouseInputSource,
      if (gamepad != null)
        { trigger: Int -> input.GamepadInputSource(gamepad.id, trigger) }
      else
        disconnectedScalarInputSource
  )
}

private var previousMousePosition = Vector2i()

fun getInputState(platformInput: PlatformInput, commands: List<HaftCommand<LabCommandType>>): LabInputState {
  val mousePosition = platformInput.getMousePosition()
  val input = LabInputState(
      commands,
      mousePosition,
      mousePosition - previousMousePosition
  )
  previousMousePosition = mousePosition
  return input
}

class LabClient(val config: LabConfig, val client: Client) {

  val globalKeyPressCommands: Map<LabCommandType, CommandHandler<LabCommandType>> = mapOf(
      LabCommandType.viewGame to { _ -> config.view = Views.game },
      LabCommandType.viewMap to { _ -> config.view = Views.map },
      LabCommandType.viewModel to { _ -> config.view = Views.model },
      LabCommandType.viewWorld to { _ -> config.view = Views.world },
      LabCommandType.viewTexture to { _ -> config.view = Views.texture }
//      LabCommandType.menu to { _ ->
//        if (config.view != "game")
//          rendering.platform.process.close()
//      }
  )
  val deviceHandlers = createLabDeviceHandlers(client.platform.input)

  fun getBindings() = labInputConfig[Views.global]!!.plus(labInputConfig[config.view]!!)

  fun updateInput(viewCommands: LabCommandMap, previousState: LabState): Pair<HaftCommands<LabCommandType>,
      InputTriggerState<LabCommandType>> {
    val (commands, nextLabInputState) = gatherInputCommands(getBindings(), deviceHandlers, previousState.labInput)
    applyCommands(commands, viewCommands.plus(globalKeyPressCommands))
    return Pair(commands, nextLabInputState)
  }

  fun prepareClient(windowInfo: WindowInfo) {
    client.renderer.prepareRender(windowInfo)
    client.platform.input.isMouseVisible(true)
    client.platform.input.update()
  }

  fun updateGame(world: World, screens: List<Screen>, previousState: LabState): LabClientResult {
    client.platform.input.isMouseVisible(false)
    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
    return updateGame(config, client, world, screens, previousState, commands, nextLabInputState)
  }

  fun updateModel(windowInfo: WindowInfo, previousState: LabState, delta: Float): LabClientResult {
    prepareClient(windowInfo)
    val view = ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())

    val layout = view.createLayout(windowInfo.dimensions, previousState.modelViewState)
    val (commands, nextLabInputState) = updateInput(view.getCommands(), previousState)
    val input = getInputState(client.platform.input, commands)
    renderLab(windowInfo, layout.boxes)
    val modelViewState = view.updateState(layout, input, previousState.modelViewState, delta)
    view.release()

    return LabClientResult(
        listOf(),
        previousState.copy(labInput = nextLabInputState, modelViewState = modelViewState),
        MenuActionType.none
    )
  }

  fun updateTexture(windowInfo: WindowInfo, previousState: LabState): LabClientResult {
    prepareClient(windowInfo)
    val view = TextureView()

    val layout = view.createLayout(client.renderer, config.textureView, windowInfo.dimensions)
    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
    val input = getInputState(client.platform.input, commands)

    renderLab(windowInfo, layout.boxes)
    updateTextureState(layout, input, config.textureView, client.renderer)
    return LabClientResult(
        listOf(),
        previousState.copy(labInput = nextLabInputState),
        MenuActionType.none
    )
  }

  fun updateWorld(windowInfo: WindowInfo, metaWorld: AbstractWorld, previousState: LabState): LabClientResult {
    prepareClient(windowInfo)
    val view = WorldView(config.worldView, metaWorld, client.renderer)

    val layout = view.createLayout(windowInfo.dimensions)
    val (_, nextLabInputState) = updateInput(view.getCommands(), previousState)

    renderLab(windowInfo, layout)
    return LabClientResult(
        listOf(),
        previousState.copy(labInput = nextLabInputState),
        MenuActionType.none
    )
  }

  fun updateMap(windowInfo: WindowInfo, world: World, previousState: LabState, delta: Float): LabClientResult {
    prepareClient(windowInfo)
    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
    val input = getInputState(client.platform.input, commands)
    val camera = createTopDownCamera(config.mapView.camera)
    updateMapState(config.mapView, world.meta, camera, input, windowInfo, delta)

    renderMapView(client, world.meta, config.mapView, camera)
    return LabClientResult(
        listOf(),
        previousState.copy(labInput = nextLabInputState),
        MenuActionType.none
    )
  }

  fun update(world: World, screens: List<Screen>, previousState: LabState, delta: Float): LabClientResult {
    val windowInfo = client.platform.display.getInfo()
    return when (config.view) {
      Views.game -> updateGame(world, screens, previousState)
      Views.model -> updateModel(windowInfo, previousState, delta)
      Views.texture -> updateTexture(windowInfo, previousState)
      Views.world -> updateWorld(windowInfo, world.meta, previousState)
      Views.map -> updateMap(windowInfo, world, previousState, delta)
      else -> throw Error("Not supported")
    }
  }

  fun renderLab(windowInfo: WindowInfo, boxes: List<Box>) {
    val canvas = createCanvas(client.renderer, windowInfo)
    renderLayout(boxes, canvas)
  }

}