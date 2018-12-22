package lab

import marloth.integration.AppState
import haft.*
import lab.views.*
import lab.views.game.updateGameView
import lab.views.game.updateLabGameState
import lab.views.map.mapLayout
import lab.views.map.updateMapState
import lab.views.model.ModelView
import lab.views.model.ModelViewState
import lab.views.world.WorldView
import marloth.clienting.Client
import marloth.clienting.newBloomInputState
import mythic.bloom.*
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import mythic.spatial.Vector2
import rendering.createCanvas
import scenery.Screen
import simulation.Realm
import simulation.World

data class LabState(
    val labInput: InputTriggerState<LabCommandType>,
    val modelViewState: ModelViewState,
    val app: AppState
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

private var previousMousePosition = Vector2()

fun getInputState(platformInput: PlatformInput, commands: List<HaftCommand<LabCommandType>>): LabCommandState {
  val mousePosition = platformInput.getMousePosition()
  val input = LabCommandState(
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
    client.platform.input.update()
  }

  fun updateGame(world: World?, screens: List<Screen>, state: LabState): LabClientResult {
    val (commands, nextLabInputState) = updateInput(mapOf(), state)
    updateLabGameState(config.gameView, commands)
    val (nextClientState, globalCommands) = updateGameView(client, world, state)

    val newLabState = state.copy(
        labInput = nextLabInputState,
        app = state.app.copy(
            client = nextClientState
        )
    )

    return LabClientResult(globalCommands, newLabState)
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
        previousState.copy(labInput = nextLabInputState, modelViewState = modelViewState)
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
        previousState.copy(labInput = nextLabInputState)
    )
  }

  fun updateWorld(windowInfo: WindowInfo, metaWorld: Realm?, previousState: LabState): LabClientResult {
    prepareClient(windowInfo)
    val view = WorldView(config.worldView, metaWorld, client.renderer)

    val layout = view.createLayout(windowInfo.dimensions)
    val boxes = layout(Seed(
        bag = previousState.app.client.bloomState.bag,
        bounds = Bounds(dimensions = windowInfo.dimensions)
    ))
    val (_, nextLabInputState) = updateInput(view.getCommands(), previousState)

    renderLab(windowInfo, boxes)
    return LabClientResult(
        listOf(),
        previousState.copy(labInput = nextLabInputState)
    )
  }

  fun updateMap(windowInfo: WindowInfo, world: World?, state: LabState, delta: Float): LabClientResult {
    prepareClient(windowInfo)
    val (commands, nextLabInputState) = updateInput(mapOf(), state)
    val layout = if (world != null) {
      val input = getInputState(client.platform.input, commands)
      updateMapState(config.mapView, world.realm, input, windowInfo, state.app.client.bloomState, delta)
      mapLayout(client, world.realm, config.mapView)
    } else
      emptyFlower

    val boxes = layout(Seed(
        bag = state.app.client.bloomState.bag,
        bounds = Bounds(dimensions = windowInfo.dimensions)
    ))
    renderLab(windowInfo, boxes)

    val bloomInputState = newBloomInputState(client.platform.input)
    val newBloomState = updateBloomState(boxes, state.app.client.bloomState, bloomInputState)

    return LabClientResult(
        listOf(),
        state.copy(
            labInput = nextLabInputState,
            app = state.app.copy(
                client = state.app.client.copy(
                    bloomState = newBloomState
                )
            )
        )
    )
  }

  fun update(world: World?, screens: List<Screen>, previousState: LabState, delta: Float): LabClientResult {
    if (config.view != Views.game) {
      client.platform.input.isMouseVisible(true)
    }

    val windowInfo = client.platform.display.getInfo()
    return when (config.view) {
      Views.game -> updateGame(world, screens, previousState)
      Views.model -> updateModel(windowInfo, previousState, delta)
      Views.texture -> updateTexture(windowInfo, previousState)
      Views.world -> updateWorld(windowInfo, world?.realm, previousState)
      Views.map -> updateMap(windowInfo, world, previousState, delta)
      else -> throw Error("Not supported")
    }
  }

  fun renderLab(windowInfo: WindowInfo, boxes: List<Box>) {
    val canvas = createCanvas(client.renderer, windowInfo)
    renderLayout(boxes, canvas)
  }

}