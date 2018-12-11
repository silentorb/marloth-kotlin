package mythic.desktop

import haft.*
import mythic.platforming.PlatformInput
import mythic.spatial.Vector2
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWScrollCallback
import java.lang.NullPointerException

val GamepadIndices = GLFW_JOYSTICK_1..GLFW_JOYSTICK_LAST

fun enumerateActiveGamepadIds(): List<Int> =
    GamepadIndices
        .filter { glfwJoystickPresent(it) }

//val deadZone = 0.15f
val deadZone = 0.2f

fun getGamepadAxes(device: Int, axisDirIndex: Int): Float {
  val axes = glfwGetJoystickAxes(device)
  return if (axes == null)
    0f
  else if (axisDirIndex < GAMEPAD_AXIS_TRIGGER_LEFT) {
    val axisIndex = axisDirIndex / 2
    val value = axes[axisIndex]

//  println(axisDirIndex.toString() + ", " + axisIndex + ", " + value + ", " + (axisDirIndex % 2))
    if (axisDirIndex % 2 == 1)
      if (value > deadZone) value else 0f
    else
      if (value < -deadZone) -value else 0f
  } else {
    val value = axes[axisDirIndex - 4]
    if (value > deadZone) value else 0f
  }
}

class DesktopInput(val window: Long) : PlatformInput {

  private var mouseScrollYBuffer: Float = 0f
  private var mouseScrollY: Float = 0f

  init {
    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
    glfwSetScrollCallback(window, GLFWScrollCallback.create({ window, xoffset, yoffset ->
      mouseScrollYBuffer = yoffset.toFloat()
    }))
  }

  override fun update() {
    mouseScrollY = mouseScrollYBuffer
    mouseScrollYBuffer = 0f
  }

  override fun getGamepads(): List<Gamepad> =
      enumerateActiveGamepadIds()
          .map { Gamepad(it, glfwGetJoystickName(it)) }

  override val KeyboardInputSource = { key: Int ->
    if (glfwGetKey(window, key) == GLFW_PRESS) {
      1f
    } else
      0f
  }

  override val GamepadInputSource = { device: Int, trigger: Int ->
    if (trigger < GAMEPAD_BUTTON_A)
      getGamepadAxes(device, trigger)
    else {
      val buttons = glfwGetJoystickButtons(device)
      if (buttons != null && buttons[trigger - GAMEPAD_BUTTON_A].toInt() == GLFW_PRESS)
        1f
      else
        0f
    }
  }

  override val MouseInputSource = { key: Int ->
    if (key < MOUSE_SKIP) {
      if (glfwGetMouseButton(window, key) == GLFW_PRESS)
        1f
      else
        0f
    } else if (key == MOUSE_SCROLL_UP) {
      if (mouseScrollY > 0)
        mouseScrollY
      else
        0f
    } else if (key == MOUSE_SCROLL_DOWN) {
      if (mouseScrollY < 0)
        -mouseScrollY
      else
        0f
    } else {
      0f
    }
  }

  override fun getMousePosition(): Vector2 {
    val tempX = DoubleArray(1)
    val tempY = DoubleArray(1)
    glfwGetCursorPos(window, tempX, tempY)
    return Vector2(tempX[0].toFloat(), tempY[0].toFloat())
  }

  override fun isMouseVisible(value: Boolean) {
    val mode = if (value == true) GLFW_CURSOR_NORMAL else GLFW_CURSOR_DISABLED
    glfwSetInputMode(window, GLFW_CURSOR, mode)
  }
}
