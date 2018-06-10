package junk_client

import mythic.bloom.*
import mythic.platforming.Platform
import mythic.spatial.toVector2
import org.joml.Vector2i
import haft.*
import mythic.platforming.WindowInfo
import org.lwjgl.glfw.GLFW

data class ClientState(
    val previousInput: InputTriggerState<CommandType>,
    val mode: GameMode,
    val abilitySelectionState: AbilitySelectionState?
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

  fun updateInput(layout: Layout, state: ClientState, userInput: UserInput): ClientState {
    val isActive = haft.isActive(userInput.commands)
    if (isActive(CommandType.select)) {
      val event = getEvent(layout, userInput.mousePosition)
      if (event != null)
        return applyInput(event, state)
    }

    return state
  }

  fun update(state: AppState, delta: Float): ClientState {
    val actualWindowInfo = getWindowInfo()
    val windowInfo = actualWindowInfo.copy(dimensions = Vector2i(320, 200))
    val canvas = createCanvas(windowInfo)
    val bounds = Bounds(dimensions = windowInfo.dimensions.toVector2())
    val layout = abilitySelectionView(state.client.abilitySelectionState!!, bounds)
    renderScreen(renderer, layout, canvas, windowInfo, getWindowInfo())
    val (userInput, triggerState) = updateInputState(state.client.previousInput, actualWindowInfo)
    return updateInput(layout, state.client.copy(previousInput = triggerState), userInput)
  }
}
