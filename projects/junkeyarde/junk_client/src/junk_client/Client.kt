package junk_client

import mythic.bloom.*
import mythic.platforming.Platform
import mythic.spatial.toVector2
import org.joml.Vector2i
import haft.*
import junk_client.views.ClientBattleState
import junk_client.views.abilitySelectionView
import junk_client.views.battleView
import mythic.platforming.WindowInfo
import org.lwjgl.glfw.GLFW

data class ClientState(
    val previousInput: InputTriggerState<CommandType>,
    val mode: GameMode,
    val shopState: ShopState?,
    val battle: ClientBattleState?
)

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()
  val bindings: Bindings<CommandType> = createStrokeBindings(1, mapOf(
      GLFW.GLFW_MOUSE_BUTTON_1 to CommandType.select
  ))

  fun getWindowInfo() = platform.display.getInfo()
  val deviceHandlers = createDeviceHandlers(platform.input)

  fun updateInputState(previousInput: InputTriggerState<CommandType>, actualWindowInfo: WindowInfo): Pair<
      UserInput, InputTriggerState<CommandType>> {
    platform.input.update()
    val (commands, nextInputState) = gatherInputCommands(bindings, deviceHandlers, previousInput)
    val initalMousePosition = platform.input.getMousePosition()
    val mousePosition = Vector2i(
        initalMousePosition.x * 320 / actualWindowInfo.dimensions.x,
        initalMousePosition.y * 200 / actualWindowInfo.dimensions.y
    )
    val userInput = UserInput(commands, mousePosition)
    return Pair(userInput, nextInputState)
  }

  fun getInputEvent(layout: Layout, userInput: UserInput): Any? {
    val isActive = haft.isActive(userInput.commands)
    if (isActive(CommandType.select)) {
      val event = getEvent(layout, userInput.mousePosition)
      if (event != null)
        return event
    }

    return null
  }

  private fun prepareLayout(state: AppState, bounds: Bounds): Layout {
    return when (state.client.mode) {
      GameMode.abilitySelection -> abilitySelectionView(state.client.shopState!!, bounds)
      GameMode.battle -> battleView(state.client.battle!!, state.world!!, bounds)
    }
  }

  fun update(state: AppState, delta: Float): Pair<ClientState, CommandType?> {
    val actualWindowInfo = getWindowInfo()
    val windowInfo = actualWindowInfo.copy(dimensions = Vector2i(320, 200))
    val canvas = createCanvas(windowInfo)
    val bounds = Bounds(dimensions = windowInfo.dimensions.toVector2())
    val (userInput, triggerState) = updateInputState(state.client.previousInput, actualWindowInfo)
    val layout = prepareLayout(state, bounds)
    renderScreen(renderer, layout, canvas, windowInfo, actualWindowInfo)
    val newClientState = state.client.copy(previousInput = triggerState)
    val event = getInputEvent(layout, userInput)
    return if (event != null)
      applyInput(event, newClientState)
    else
      Pair(newClientState, null)
  }
}
