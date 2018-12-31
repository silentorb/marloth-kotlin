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

data class InputDeviceState(
    val events: List<InputEvent>,
    val mousePosition: Vector2
)

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

typealias BindingSourceTarget = Int
typealias BindingSource<T> = (InputEvent) -> Pair<Binding<T>, BindingSourceTarget>?

fun matches(event: InputEvent): (InputEvent) -> Boolean = { other ->
  event.device == other.device && event.index == other.index
}

fun <T> mapEventsToCommands(deviceStates: List<InputDeviceState>, strokes: Set<T>, getBinding: BindingSource<T>): HaftCommands<T> =
    deviceStates.last().events
        .mapNotNull { event ->
          val bindingPair = getBinding(event)
          if (bindingPair != null) {
            val (binding, index) = bindingPair
            val isStroke = strokes.contains(binding.command)
            if (!isStroke || deviceStates.dropLast(1).last().events.none(matches(event)))
              HaftCommand(
                  type = binding.command,
                  target = index,
                  value = event.value
              )
            else
              null
          } else
            null
        }

fun <T> getBindingSimple(bindings: List<Binding<T>>): BindingSource<T> = { event ->
  val binding = bindings.firstOrNull {
    val values = DeviceIndex.values()
    it.device == values[Math.min(2, event.device)] && it.trigger == event.index
  }
  if (binding != null)
    Pair(binding, 0)
  else
    null
}

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
