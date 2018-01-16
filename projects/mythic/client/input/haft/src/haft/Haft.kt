package haft

import mythic.platforming.Input

enum class TriggerLifetime {
  pressed,
  end
}

data class TriggerState(
    val lifetime: TriggerLifetime,
    val value: Float
)

interface DeviceHandler {
  fun getState(trigger: Int, previousState: TriggerState?): TriggerState?
}

data class Binding<T>(
    val device: Int,
    val trigger: Int,
    val type: T,
    val target: Int
)

data class Command<T>(
    val type: T,
    val target: Int,
    val value: Float,
    val lifetime: TriggerLifetime
)

typealias Commands<T> = List<Command<T>>

typealias CommandHandler<T> = (Command<T>) -> Unit

typealias InputState<T> = Map<Binding<T>, TriggerState?>

typealias Bindings<T> = List<Binding<T>>

class UnusedDeviceHandler() : DeviceHandler {
  override fun getState(trigger: Int, previousState: TriggerState?): TriggerState? = TriggerState(TriggerLifetime.end, 0f)
}

class KeyboardDeviceHandler(val input: Input) : DeviceHandler {

  override fun getState(trigger: Int, previousState: TriggerState?): TriggerState? {
    if (input.isKeyPressed(trigger))
      return TriggerState(TriggerLifetime.pressed, 1f)

    if (previousState != null && previousState.lifetime == TriggerLifetime.pressed)
      return TriggerState(TriggerLifetime.end, 1f)

    return null
  }
}

fun <T> getCurrentInputState(bindings: Bindings<T>, handlers: List<DeviceHandler>, previousState: InputState<T>): InputState<T> =
    bindings.associate { Pair(it, handlers[it.device].getState(it.trigger, previousState[it])) }

fun <T> createEmptyInputState(bindings: Bindings<T>): InputState<T> =
    bindings.associate { Pair(it, null) }

fun <T> gatherCommands(state: InputState<T>): Commands<T> {
  return state
      .filter({ it.value != null })
      .map({ Command<T>(it.key.type, it.key.target, it.value!!.value, it.value!!.lifetime) })
}

fun createDeviceHandlers(input: Input): List<DeviceHandler> {
  return listOf(
      KeyboardDeviceHandler(input)
  )
}

class InputManager<T>(val bindings: Bindings<T>, val deviceHandlers: List<DeviceHandler>) {
  var inputState = createEmptyInputState(bindings)

  fun update(): Commands<T> {
    inputState = getCurrentInputState(bindings, deviceHandlers, inputState)
    return gatherCommands(inputState)
  }
}

fun <T> handleKeystrokeCommands(commands: Commands<T>, keyStrokeCommands: Map<T, (Command<T>) -> Unit>) {
  commands.filter({ keyStrokeCommands.containsKey(it.type) && it.lifetime == TriggerLifetime.end })
      .forEach({ keyStrokeCommands[it.type]!!(it) })
}

fun <T> createBindings(device: Int, target: Int, bindings: Map<Int, T>) =
    bindings.map({ Binding(device, it.key, it.value, target) })