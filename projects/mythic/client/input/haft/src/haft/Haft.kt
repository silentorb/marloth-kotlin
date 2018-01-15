package haft

import commanding.Command
import commanding.CommandLifetime
import commanding.CommandType
import commanding.Commands
import mythic.platforming.Input

data class TriggerState(
    val lifetime: CommandLifetime,
    val value: Float
)

interface DeviceHandler {
  fun getState(trigger: Int, previousState: TriggerState?): TriggerState?
}

data class Binding(
    val device: Int,
    val trigger: Int,
    val type: CommandType,
    val target: Int
)

typealias InputState = Map<Binding, TriggerState?>

typealias Bindings = List<Binding>

class UnusedDeviceHandler() : DeviceHandler {
  override fun getState(trigger: Int, previousState: TriggerState?): TriggerState? = TriggerState(CommandLifetime.end, 0f)
}

class KeyboardDeviceHandler(val input: Input) : DeviceHandler {

  override fun getState(trigger: Int, previousState: TriggerState?): TriggerState? {
    if (input.isKeyPressed(trigger))
      return TriggerState(CommandLifetime.pressed, 1f)

    if (previousState != null && previousState.lifetime == CommandLifetime.pressed)
      return TriggerState(CommandLifetime.end, 1f)

    return null
  }
}

fun getCurrentInputState(bindings: Bindings, handlers: List<DeviceHandler>, previousState: InputState): InputState =
    bindings.associate { Pair(it, handlers[it.device].getState(it.trigger, previousState[it])) }

fun createEmptyInputState(bindings: Bindings): InputState =
    bindings.associate { Pair(it, null) }

fun gatherCommands(state: InputState): Commands {
  return state
      .filter({ it.value != null })
      .map({ Command(it.key.type, it.key.target, it.value!!.value, it.value!!.lifetime) })
}

//fun gatherCommands(bindings: Bindings, handlers: List<DeviceHandler>, previousState: InputState): Commands {
//  val currentState = getCurrentInputState(bindings, handlers, previousState)
//  return gatherCommands(currentState)
//}

fun createDeviceHandlers(input: Input): List<DeviceHandler> {
  return listOf(
      KeyboardDeviceHandler(input)
  )
}
