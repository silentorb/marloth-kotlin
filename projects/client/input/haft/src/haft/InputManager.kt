package haft

import commanding.Command
import commanding.CommandType
import commanding.Commands
import org.lwjgl.glfw.GLFW.*

typealias EventMap = Map<Int, CommandType>

data class InputMap(
    val keyboard: EventMap,
    val mouse: EventMap,
    val gamepad1: EventMap,
    val gamepad2: EventMap
)

class InputManager(val window: Long) {

  init {
    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
  }

  private fun isPressed(key: Int): Boolean {
    return glfwGetKey(window, key) == GLFW_PRESS
  }

  private fun getKeyboardCommands(keyboardMap: EventMap): Commands {
    return keyboardMap.filter({ isPressed(it.key) })
        .values.map({ Command(it, 1f) }).toTypedArray()
  }

  fun getCommands(inputMap: InputMap): Commands {
    return getKeyboardCommands(inputMap.keyboard)
  }
}