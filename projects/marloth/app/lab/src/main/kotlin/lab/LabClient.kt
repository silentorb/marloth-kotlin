package lab

import commanding.CommandType
import lab.views.*
import marloth.clienting.Client
import mythic.platforming.PlatformInput
import mythic.platforming.WindowInfo
import scenery.GameScene
import simulation.AbstractWorld
import haft.*
import lab.views.map.renderMapView
import lab.views.map.updateMapState
import lab.views.model.ModelView
import lab.views.model.ModelViewState
import lab.views.world.WorldView
import marloth.clienting.ClientState
import marloth.clienting.InputState
import marloth.clienting.gui.MenuActionType
import marloth.clienting.gui.menuButtonAction
import marloth.clienting.gui.updateMenuState
import mythic.bloom.Box
import mythic.bloom.renderLayout
import org.joml.Vector2i
import org.joml.minus
import rendering.createCanvas

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

//fun selectView(config: LabConfig, abstractWorld: AbstractWorld, client: Client, view: String): View =
//    when (view) {
//      "game" -> GameView(config.gameView)
//      "world" -> WorldView(config.worldView, abstractWorld, client.renderer)
//      "model" -> ModelView(config.modelView, client.renderer, client.platform.input.getMousePosition())
//      "texture" -> TextureView()
//      else -> throw Error("Not supported")
//    }

private var previousMousePosition = Vector2i()

fun getInputState(platformInput: PlatformInput, commands: List<Command<LabCommandType>>): LabInputState {
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
//          client.platform.process.close()
//      }
  )
  val deviceHandlers = createLabDeviceHandlers(client.platform.input)

  fun updateInput(viewCommands: LabCommandMap, previousState: LabState): Pair<Commands<LabCommandType>,
      InputTriggerState<LabCommandType>> {
    val (commands, nextLabInputState) = gatherInputCommands(getBindings(), deviceHandlers, previousState.labInput)
    applyCommands(commands, viewCommands.plus(globalKeyPressCommands))
    return Pair(commands, nextLabInputState)
  }

  fun getBindings() = labInputConfig[Views.global]!!.plus(labInputConfig[config.view]!!)

  fun prepareClient(windowInfo: WindowInfo) {
    client.renderer.prepareRender(windowInfo)
    client.platform.input.isMouseVisible(true)
    client.platform.input.update()
  }

  fun updateGame(windowInfo: WindowInfo, scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): LabClientResult {
    client.platform.input.isMouseVisible(false)
    client.platform.input.update()
//    println(client.platform.input.getMousePosition())
    val view = GameView(config.gameView)
    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
    val input = getInputState(client.platform.input, commands)
    view.updateState(input, delta)
    val properties = client.input.prepareInput(previousState.gameInput, scenes.map { it.player })
    val mainEvents = client.input.updateGameInput1(properties, previousState.gameClientState)
//    client.updateGameInput(properties, client.playerInputProfiles)

    val waitingEvents = client.input.checkForNewGamepads1(properties)

    val allCommands = client.input.updateGameInput2(mainEvents)
        .plus(client.input.checkForNewGamepads2(waitingEvents, properties.players.size))
    val menuCommands = filterKeystrokeCommands(allCommands)
    val newMenuState = updateMenuState(previousState.gameClientState.menu, menuCommands)
    val menuAction = menuButtonAction(newMenuState, menuCommands)
    client.handleMenuAction(menuAction)
    renderScene(client, GameViewRenderData(scenes, metaWorld, config.gameView, previousState.gameClientState.menu))

    val newInputState = InputState(
        events = mainEvents.plus(waitingEvents),
        mousePosition = client.platform.input.getMousePosition()
    )

    val newGameClientState = ClientState(
        menu = newMenuState,
        input = newInputState
    )

    val newLabState = LabState(
        labInput = nextLabInputState,
        gameInput = mainEvents.plus(waitingEvents),
        gameClientState = newGameClientState,
        modelViewState = previousState.modelViewState
    )

    return LabClientResult(allCommands, newLabState, menuAction)
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

  fun updateMap(windowInfo: WindowInfo, scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): LabClientResult {
    prepareClient(windowInfo)
    val (commands, nextLabInputState) = updateInput(mapOf(), previousState)
    val input = getInputState(client.platform.input, commands)
    updateMapState(config.mapView, input, delta)

    renderMapView(client, metaWorld, config.mapView)
    return LabClientResult(
        listOf(),
        previousState.copy(labInput = nextLabInputState),
        MenuActionType.none
    )
  }

  fun update(scenes: List<GameScene>, metaWorld: AbstractWorld, previousState: LabState, delta: Float): LabClientResult {
    val windowInfo = client.platform.display.getInfo()
    return when (config.view) {
      Views.game -> updateGame(windowInfo, scenes, metaWorld, previousState, delta)
      Views.model -> updateModel(windowInfo, previousState, delta)
      Views.texture -> updateTexture(windowInfo, previousState)
      Views.world -> updateWorld(windowInfo, metaWorld, previousState)
      Views.map -> updateMap(windowInfo, scenes, metaWorld, previousState, delta)
      else -> throw Error("Not supported")
    }
  }

  fun renderLab(windowInfo: WindowInfo, boxes: List<Box>) {
    val canvas = createCanvas(client.renderer, windowInfo)
    renderLayout(boxes, canvas)
  }

}