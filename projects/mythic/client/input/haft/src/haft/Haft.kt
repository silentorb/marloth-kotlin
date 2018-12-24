package haft

import mythic.platforming.InputEvent
import mythic.spatial.Vector2

//enum class TriggerLifetime {
//  pressed,
//  end
//}

//data class TriggerState(
//    val lifetime: TriggerLifetime,
//    val value: Float
//)

enum class DeviceIndex {
  keyboard,
  mouse,
  gamepad
}

data class Binding<T>(
    val device: DeviceIndex,
    val trigger: Int,
    val command: T
)

data class HaftCommand<T>(
    val type: T,
    val target: Int,
    val value: Float
)

fun <T> simpleCommand(type: T, target: Int = 0): HaftCommand<T> =
    HaftCommand(
        type = type,
        target = target,
        value = 1f
    )

data class Gamepad(val id: Int, val name: String)

typealias HaftCommands<T> = List<HaftCommand<T>>

typealias CommandHandler<T> = (HaftCommand<T>) -> Unit

//typealias InputTriggerState<T> = Map<Binding<T>, TriggerState?>

typealias Bindings<T> = List<Binding<T>>

typealias ScalarInputSource = (trigger: Int) -> Float

typealias MultiDeviceScalarInputSource = (device: Int, trigger: Int) -> Float

typealias InputProfiles<T> = List<Bindings<T>>

val disconnectedScalarInputSource: ScalarInputSource = { 0f }

const val MouseMovementLeft = 5
const val MouseMovementRight = 6
const val MouseMovementUp = 7
const val MouseMovementDown = 8

fun applyMouseAxis(device: Int, value: Float, firstIndex: Int, secondIndex: Int, scale: Float) =
    if (value > 0)
      InputEvent(device, firstIndex, value * scale)
    else if (value < 0)
      InputEvent(device, secondIndex, -value * scale)
    else
      null

fun applyMouseMovement(device: Int, mouseOffset: Vector2): List<InputEvent> =
    listOfNotNull(
        applyMouseAxis(device, mouseOffset.x, MouseMovementRight, MouseMovementLeft, 1f),
        applyMouseAxis(device, mouseOffset.y, MouseMovementDown, MouseMovementUp, 1f)
    )

//fun getState(source: ScalarInputSource, trigger: Int, previousState: TriggerState?): TriggerState? {
//  val value = source(trigger)
//  if (value != 0f)
//    return TriggerState(TriggerLifetime.pressed, value)
//
//  if (previousState != null && previousState.lifetime == TriggerLifetime.pressed)
//    return TriggerState(TriggerLifetime.end, value)
//
//  return null
//}

//fun <T> getCurrentInputState(bindings: Bindings<T>, handlers: List<ScalarInputSource>,
//                             previousState: InputTriggerState<T>): InputTriggerState<T> =
//    bindings.associate { Pair(it, getState(handlers[it.device], it.trigger, previousState[it])) }
//
//fun <T> createEmptyInputState(bindings: Bindings<T>): InputTriggerState<T> =
//    bindings.associate { Pair(it, null) }
//
//fun <T> gatherCommands(state: InputTriggerState<T>): HaftCommands<T> {
//  return state
//      .filter({ it.value != null && (!it.key.isStroke || it.value!!.lifetime == TriggerLifetime.end) })
//      .map({
//        HaftCommand<T>(
//            type = it.key.type,
//            target = it.key.target,
//            value = it.value!!.value,
//            lifetime = it.value!!.lifetime
//        )
//      })
//}
//
////typealias ProfileStates<T> = Map<Bindings<T>, InputTriggerState<T>>
//
//fun <T> gatherProfileCommands2(profiles: List<Bindings<T>>, profileStates: ProfileStates<T>,
//                               deviceHandlers: List<ScalarInputSource>): ProfileStates<T> {
//  val previous = profiles.associate { profile ->
//    Pair(profile, profileStates[profile] ?: createEmptyInputState(profile))
//  }
//
//  return profiles.associate { profile ->
//    Pair(profile, getCurrentInputState(profile, deviceHandlers, previous[profile]!!))
//  }
//}
//
//fun <T> gatherProfileCommands3(states: ProfileStates<T>): HaftCommands<T> =
//    states.flatMap { gatherCommands(it.value) }
//
//fun <T> gatherInputCommands(bindings: Bindings<T>, deviceHandlers: List<ScalarInputSource>,
//                            previousState: InputTriggerState<T>?): Pair<HaftCommands<T>, InputTriggerState<T>> {
//  val previous = previousState ?: createEmptyInputState(bindings)
//  val next = getCurrentInputState(bindings, deviceHandlers, previous)
//  return Pair(gatherCommands(next), next)
//}

//fun <T> filterKeystrokeCommands(commands: HaftCommands<T>) =
//    commands.filter({ it.lifetime == TriggerLifetime.end })
//
//fun <T> filterKeystrokeCommands(commands: HaftCommands<T>, bindings: List<T>) =
//    commands.filter({ bindings.contains(it.type) && it.lifetime == TriggerLifetime.end })
//
//fun <T> handleKeystrokeCommands(commands: HaftCommands<T>, keyStrokeCommands: Map<T, (HaftCommand<T>) -> Unit>) {
//  commands.filter({ keyStrokeCommands.containsKey(it.type) && it.lifetime == TriggerLifetime.end })
//      .forEach({ keyStrokeCommands[it.type]!!(it) })
//}

fun <T> applyCommands(commands: HaftCommands<T>, actions: Map<T, (HaftCommand<T>) -> Unit>) {
  commands.filter({ actions.containsKey(it.type) })
      .forEach({ actions[it.type]!!(it) })
}

fun <T> createBindings(device: DeviceIndex, bindings: Map<Int, T>) =
    bindings.map({ Binding(device, it.key, it.value) })

fun <T> isActive(commands: List<HaftCommand<T>>, commandType: T): Boolean =
    commands.any { it.type == commandType }

fun <T> isActive(commands: List<HaftCommand<T>>): (T) -> Boolean =
    { commandType -> haft.isActive(commands, commandType) }

fun <T> getCommand(commands: List<HaftCommand<T>>, commandType: T) =
    commands.first { it.type == commandType }

//data class InputResult<T>(
//    val commands: HaftCommands<T>,
//    val inputState: ProfileStates<T>) {
//
//  operator fun plus(second: InputResult<T>) =
//      InputResult<T>(commands.plus(second.commands), inputState.plus(second.inputState))
//}
