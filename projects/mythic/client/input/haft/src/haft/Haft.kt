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

data class HaftCommand<T>(
    val type: T,
    val target: Int,
    val value: Float,
    val lifetime: TriggerLifetime
)

data class Gamepad(val id: Int, val name: String)

typealias HaftCommands<T> = List<HaftCommand<T>>

typealias CommandHandler<T> = (HaftCommand<T>) -> Unit

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

fun <T> gatherCommands(state: InputTriggerState<T>): HaftCommands<T> {
  return state
      .filter({ it.value != null && (!it.key.isStroke || it.value!!.lifetime == TriggerLifetime.end) })
      .map({ HaftCommand<T>(it.key.type, it.key.target, it.value!!.value, it.value!!.lifetime) })
}

typealias ProfileStates<T> = Map<Bindings<T>, InputTriggerState<T>>

typealias InputProfileResult<T> = Pair<HaftCommands<T>, ProfileStates<T>>

//data class HaftInputState<T>(
//    val profileStates: ProfileStates<T>
//)

fun <T> gatherProfileCommands(profiles: List<Bindings<T>>, profileStates: ProfileStates<T>,
                              deviceHandlers: List<ScalarInputSource>): InputProfileResult<T> {
  val previous = profiles.associate { profile ->
    Pair(profile, profileStates[profile] ?: createEmptyInputState(profile))
  }

  val next = profiles.associate { profile ->
    Pair(profile, getCurrentInputState(profile, deviceHandlers, previous[profile]!!))
  }
  return Pair(next.flatMap { gatherCommands(it.value) }, next)
}

fun <T> gatherProfileCommands2(profiles: List<Bindings<T>>, profileStates: ProfileStates<T>,
                              deviceHandlers: List<ScalarInputSource>): ProfileStates<T> {
  val previous = profiles.associate { profile ->
    Pair(profile, profileStates[profile] ?: createEmptyInputState(profile))
  }

  return profiles.associate { profile ->
    Pair(profile, getCurrentInputState(profile, deviceHandlers, previous[profile]!!))
  }
}

fun <T> gatherProfileCommands3(states: ProfileStates<T>): HaftCommands<T> =
    states.flatMap { gatherCommands(it.value) }

fun <T> gatherInputCommands(bindings: Bindings<T>, deviceHandlers: List<ScalarInputSource>,
                            previousState: InputTriggerState<T>?): Pair<HaftCommands<T>, InputTriggerState<T>> {
  val previous = previousState ?: createEmptyInputState(bindings)
  val next = getCurrentInputState(bindings, deviceHandlers, previous)
  return Pair(gatherCommands(next), next)
}

fun <T> filterKeystrokeCommands(commands: HaftCommands<T>) =
    commands.filter({ it.lifetime == TriggerLifetime.end })

fun <T> filterKeystrokeCommands(commands: HaftCommands<T>, bindings: List<T>) =
    commands.filter({ bindings.contains(it.type) && it.lifetime == TriggerLifetime.end })

fun <T> handleKeystrokeCommands(commands: HaftCommands<T>, keyStrokeCommands: Map<T, (HaftCommand<T>) -> Unit>) {
  commands.filter({ keyStrokeCommands.containsKey(it.type) && it.lifetime == TriggerLifetime.end })
      .forEach({ keyStrokeCommands[it.type]!!(it) })
}

fun <T> applyCommands(commands: HaftCommands<T>, actions: Map<T, (HaftCommand<T>) -> Unit>) {
  commands.filter({ actions.containsKey(it.type) })
      .forEach({ actions[it.type]!!(it) })
}

fun <T> createBindings(device: Int, target: Int, bindings: Map<Int, T>) =
    bindings.map({ Binding(device, it.key, it.value, target) })

fun <T> createBindings(device: Int, bindings: Map<Int, T>) = createBindings(device, 0, bindings)

fun <T> createStrokeBindings(device: Int, target: Int, bindings: Map<Int, T>) =
    bindings.map({ Binding(device, it.key, it.value, target, true) })

fun <T> createStrokeBindings(device: Int, bindings: Map<Int, T>) = createStrokeBindings(device, 0, bindings)

fun <T> isActive(commands: List<HaftCommand<T>>, commandType: T) =
    commands.any { it.type == commandType }

fun <T> isActive(commands: List<HaftCommand<T>>) =
    { commandType: T -> haft.isActive(commands, commandType) }

fun <T> getCommand(commands: List<HaftCommand<T>>, commandType: T) =
    commands.first { it.type == commandType }

data class InputResult<T>(
    val commands: HaftCommands<T>,
    val inputState: ProfileStates<T>) {

  operator fun plus(second: InputResult<T>) =
      InputResult<T>(commands.plus(second.commands), inputState.plus(second.inputState))
}
