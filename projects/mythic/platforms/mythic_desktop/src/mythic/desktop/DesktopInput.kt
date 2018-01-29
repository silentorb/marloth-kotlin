package mythic.desktop

import haft.GAMEPAD_BUTTON_A
import haft.Gamepad
import mythic.platforming.Input
import org.lwjgl.glfw.GLFW.*

val GamepadIndices = GLFW_JOYSTICK_1..GLFW_JOYSTICK_LAST

fun enumerateActiveGamepadIds(): List<Int> =
    GamepadIndices
        .filter { glfwJoystickPresent(it) }

val deadZone = 0.15f

fun getGamepadAxes(device: Int, axisDirIndex: Int): Float {
  val axes = glfwGetJoystickAxes(device)
  val axisIndex = axisDirIndex / 2
  val value = axes[axisIndex]
//  if (value < deadZone && value > -deadZone) return 0f
//  println(axisDirIndex.toString() + ", " + axisIndex + ", " + value + ", " + (axisDirIndex % 2))
  return if (axisDirIndex % 2 == 1)
    if (value > deadZone) value else 0f
  else
    if (value < -deadZone) -value else 0f
}

class DesktopInput(val window: Long) : Input {

  init {
    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
  }

  override fun getGamepads(): List<Gamepad> =
      enumerateActiveGamepadIds()
          .map { Gamepad(it, glfwGetJoystickName(it)) }

  override val KeyboardInputSource = { key: Int ->
    if (key == 0) {
      val k = 0
    }
    if (glfwGetKey(window, key) == GLFW_PRESS)
      1f
    else
      0f
  }

  override val GamepadInputSource = { device: Int, trigger: Int ->
    if (trigger < GAMEPAD_BUTTON_A)
      getGamepadAxes(device, trigger)
    else
      if (glfwGetJoystickButtons(device)[trigger - GAMEPAD_BUTTON_A].toInt() == GLFW_PRESS) 1f else 0f
  }
}
