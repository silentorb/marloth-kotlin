package haft

val maxAxisCount = 100

val GAMEPAD_AXIS_LEFT_LEFT = 0
val GAMEPAD_AXIS_LEFT_RIGHT = 1
val GAMEPAD_AXIS_LEFT_UP = 2
val GAMEPAD_AXIS_LEFT_DOWN = 3

val GAMEPAD_AXIS_RIGHT_LEFT = 4
val GAMEPAD_AXIS_RIGHT_RIGHT = 5
val GAMEPAD_AXIS_RIGHT_UP = 6
val GAMEPAD_AXIS_RIGHT_DOWN = 7

val GAMEPAD_AXIS_TRIGGER_LEFT = 8
val GAMEPAD_AXIS_TRIGGER_RIGHT = 9

val GAMEPAD_BUTTON_A = 100
val GAMEPAD_BUTTON_B = 101
val GAMEPAD_BUTTON_X = 102
val GAMEPAD_BUTTON_Y = 103
val GAMEPAD_BUTTON_LEFT_BUMPER = 104
val GAMEPAD_BUTTON_RIGHT_BUMPER = 105
val GAMEPAD_BUTTON_BACK = 106
val GAMEPAD_BUTTON_START = 107
val GAMEPAD_BUTTON_GUIDE = 108
val GAMEPAD_BUTTON_LEFT_THUMB = 109
val GAMEPAD_BUTTON_RIGHT_THUMB = 110
val GAMEPAD_BUTTON_DPAD_UP = 111
val GAMEPAD_BUTTON_DPAD_RIGHT = 112
val GAMEPAD_BUTTON_DPAD_DOWN = 113
val GAMEPAD_BUTTON_DPAD_LEFT = 114

enum class TriggerLifetime {
  pressed,
  end
}

data class TriggerState(
    val lifetime: TriggerLifetime,
    val value: Float
)

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

data class Gamepad(val id: Int, val name: String)

typealias Commands<T> = List<Command<T>>

typealias CommandHandler<T> = (Command<T>) -> Unit

typealias InputState<T> = Map<Binding<T>, TriggerState?>

typealias Bindings<T> = List<Binding<T>>

typealias ScalarInputSource = (trigger: Int) -> Float

typealias MultiDeviceScalarInputSource = (device: Int, trigger: Int) -> Float

data class InputProfile<T>(
    val target: Int,
    val bindings: Bindings<T>
)

typealias InputProfiles<T> = List<InputProfile<T>>

fun getState(source: ScalarInputSource, trigger: Int, previousState: TriggerState?): TriggerState? {
  val value = source(trigger)
  if (value != 0f)
    return TriggerState(TriggerLifetime.pressed, value)

  if (previousState != null && previousState.lifetime == TriggerLifetime.pressed)
    return TriggerState(TriggerLifetime.end, value)

  return null
}

fun <T> getCurrentInputState(bindings: Bindings<T>, handlers: List<ScalarInputSource>, previousState: InputState<T>): InputState<T> =
    bindings.associate { Pair(it, getState(handlers[it.device], it.trigger, previousState[it])) }

fun <T> createEmptyInputState(bindings: Bindings<T>): InputState<T> =
    bindings.associate { Pair(it, null) }

fun <T> gatherCommands(state: InputState<T>): Commands<T> {
  return state
      .filter({ it.value != null })
      .map({ Command<T>(it.key.type, it.key.target, it.value!!.value, it.value!!.lifetime) })
}

class InputManager<T>(val bindings: Bindings<T>, val deviceHandlers: List<ScalarInputSource>) {
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