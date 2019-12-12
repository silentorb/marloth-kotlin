package compuquest_client

import DeviceMap
import PlayerDevice
import mythic.bloom.*
import mythic.platforming.Platform
import haft.*
import compuquest_client.views.ClientBattleState
import compuquest_client.views.shopView
import compuquest_client.views.battleView
import compuquest_simulation.GameCommand
import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import mythic.bloom.next.Seed
import mythic.platforming.WindowInfo

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val inputProfile: Map<BloomId, BloomId>,
    val deviceMap: DeviceMap
)

data class ClientState(
    val bloomState: BloomState,
    val input: InputState,
    val mode: GameMode,
    val shopState: ShopState?,
    val battle: ClientBattleState?,
    val render: RenderState
)

fun newInputState() =
    InputState(
        deviceStates = listOf(newInputDeviceState()),
        inputProfile = mapOf(),
        deviceMap = mapOf(
            0 to PlayerDevice(1, DeviceIndex.keyboard),
            1 to PlayerDevice(1, DeviceIndex.mouse)
        )
    )

//class Client(val platform: Platform) {
//  val renderer: Renderer = Renderer()

//val bindings: Bindings<CommandType> = createStrokeBindings(1, mapOf(
//    GLFW.GLFW_MOUSE_BUTTON_1 to CommandType.select
//))

//fun getWindowInfo() = platform.display.getInfo()
//val deviceHandlers = createDeviceHandlers(platform.input)

//  fun updateInputState(input: PlatformInput, inputState: InputState, actualWindowInfo: WindowInfo): Pair<
//      UserInput, InputTriggerState<CommandType>> {
//    val newDeviceStates = updateInputDeviceStates(platform.input, inputState.deviceStates)
//    state.copy(
//        input = state.input.copy(
//            deviceStates = newDeviceStates
//        )
//    )
//    val (commands, nextInputState) = gatherInputCommands(bindings, deviceHandlers, previousInput)
//    val initalMousePosition = platform.input.getMousePosition()
//    val clippedMousePosition = Vector2(
//        minMax(initalMousePosition.x, 0f, actualWindowInfo.dimensions.x.toFloat()),
//        minMax(initalMousePosition.y, 0f, actualWindowInfo.dimensions.y.toFloat())
//    )
//    val mousePosition = Vector2(
//        clippedMousePosition.x * 320 / actualWindowInfo.dimensions.x,
//        clippedMousePosition.y * 200 / actualWindowInfo.dimensions.y
//    )
//    val userInput = UserInput(commands, mousePosition)
//    return Pair(userInput, nextInputState)
//  }

//  fun getInputEvent(layout: LayoutOld, userInput: UserInput): Any? {
//    val isActive = isActive(userInput.commands)
//    if (isActive(CommandType.select)) {
//      val event = getEvent(layout, userInput.mousePosition.toVector2i())
//      if (event != null)
//        return event
//    }
//
//    return null
//  }

private fun prepareLayout(state: AppState): Flower {
  return when (state.client.mode) {
    GameMode.abilitySelection -> shopView(state.client.shopState!!)
    GameMode.battle -> battleView(state.client.battle!!, state.world!!)
  }
}

fun updateBattleState(state: ClientBattleState?, delta: Float): ClientBattleState? =
    if (state != null)
      state.copy(
          flicker = (state.flicker + 4f * delta) % 1f
      )
    else null

fun applyLayout(layout: Flower, bloomState: BloomState, windowInfo: WindowInfo): Box {
  val seed = Seed(
      bag = bloomState.bag,
      dimensions = windowInfo.dimensions
  )
  return layout(seed)
}

fun updateClient(platform: Platform, state: AppState, delta: Float): Pair<ClientState, GameCommand?> {
  val actualWindowInfo = platform.display.getInfo()
  val windowInfo = actualWindowInfo.copy(dimensions = state.client.render.windowLowSize)
  val canvas = createCanvas(windowInfo)
//    val (userInput, triggerState) = updateInputState(state.client.previousInput, actualWindowInfo)
  val layout = prepareLayout(state)
  val box = applyLayout(layout, state.client.bloomState, windowInfo)
  renderScreen(state.client.render, box, canvas, windowInfo, actualWindowInfo)
  val newClientState = state.client.copy(
//        previousInput = triggerState,
      battle = updateBattleState(state.client.battle, delta)
  )
//    val event = getInputEvent(layout, userInput)
//    return if (event != null)
//      applyInput(event, newClientState, state.world?.activeCreatureId)
//    else
  return Pair(newClientState, null)
}
