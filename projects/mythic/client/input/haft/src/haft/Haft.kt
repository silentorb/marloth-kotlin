package haft

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
    val target: Int,
    val isStroke: Boolean = false
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

typealias InputTriggerState<T> = Map<Binding<T>, TriggerState?>

typealias Bindings<T> = List<Binding<T>>

typealias ScalarInputSource = (trigger: Int) -> Float

typealias MultiDeviceScalarInputSource = (device: Int, trigger: Int) -> Float

typealias InputProfiles<T> = List<Bindings<T>>

val disconnectedScalarInputSource: ScalarInputSource = { 0f }

fun getState(source: ScalarInputSource, trigger: Int, previousState: TriggerState?): TriggerState? {
  val value = source(trigger)
  if (value != 0f)
    return TriggerState(TriggerLifetime.pressed, value)

  if (previousState != null && previousState.lifetime == TriggerLifetime.pressed)
    return TriggerState(TriggerLifetime.end, value)

  return null
}

fun <T> getCurrentInputState(bindings: Bindings<T>, handlers: List<ScalarInputSource>,
                             previousState: InputTriggerState<T>): InputTriggerState<T> =
    bindings.associate { Pair(it, getState(handlers[it.device], it.trigger, previousState[it])) }

fun <T> createEmptyInputState(bindings: Bindings<T>): InputTriggerState<T> =
    bindings.associate { Pair(it, null) }

fun <T> gatherCommands(state: InputTriggerState<T>): Commands<T> {
  return state
      .filter({ it.value != null && (!it.key.isStroke || it.value!!.lifetime == TriggerLifetime.end) })
      .map({ Command<T>(it.key.type, it.key.target, it.value!!.value, it.value!!.lifetime) })
}

typealias ProfileStates<T> = Map<Bindings<T>, InputTriggerState<T>>

typealias InputProfileResult<T> = Pair<Commands<T>, ProfileStates<T>>

data class HaftInputState<T>(
    val profileStates: ProfileStates<T>,
    val gamepadSlots: GamepadSlots
)

fun <T> gatherInputCommands(profiles: List<Bindings<T>>, profileStates: ProfileStates<T>,
                            deviceHandlers: List<ScalarInputSource>): InputProfileResult<T> {
  val previous = profiles.associate { profile ->
    Pair(profile, profileStates[profile] ?: createEmptyInputState(profile))
  }

  val next = profiles.associate { profile ->
    Pair(profile, getCurrentInputState(profile, deviceHandlers, previous[profile]!!))
  }
  return Pair(next.flatMap { gatherCommands(it.value) }, next)
}

fun <T> handleKeystrokeCommands(commands: Commands<T>, keyStrokeCommands: Map<T, (Command<T>) -> Unit>) {
  commands.filter({ keyStrokeCommands.containsKey(it.type) && it.lifetime == TriggerLifetime.end })
      .forEach({ keyStrokeCommands[it.type]!!(it) })
}

fun <T> createBindings(device: Int, target: Int, bindings: Map<Int, T>) =
    bindings.map({ Binding(device, it.key, it.value, target) })

fun <T> createStrokeBindings(device: Int, target: Int, bindings: Map<Int, T>) =
    bindings.map({ Binding(device, it.key, it.value, target, true) })
