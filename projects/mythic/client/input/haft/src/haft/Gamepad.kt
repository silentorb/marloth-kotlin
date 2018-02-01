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

typealias GamepadSlotId = Int
typealias GamepadDeviceId = Int
typealias GamepadMap = Map<GamepadDeviceId, GamepadSlotId>

fun updateGamepadMap(previousMap: GamepadMap, gamepads: List<Int>): GamepadMap {
  val existing = previousMap.filter { gamepads.contains(it.key) }
  return existing
      .plus(gamepads
          .filter { !previousMap.containsKey(it) }
          .mapIndexed { index, entry -> Pair() }
      )
}
