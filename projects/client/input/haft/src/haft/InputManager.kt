package haft

import commanding.Command
import commanding.CommandType
import commanding.Commands
import org.lwjgl.glfw.GLFW.*

data class Binding(val type: CommandType, val target: Int)

typealias BindingMap = Map<Int, Binding>

interface DeviceHandler {
  fun getValue(event: Int): Float
}

data class DeviceInput(
    val name: String,
    val handler: DeviceHandler,
    val bindings: BindingMap
)

typealias InputRoot = Array<DeviceInput>

class UnusedDeviceHandler() : DeviceHandler {
  override fun getValue(event: Int): Float = 0f
}

class KeyboardDeviceHandler(val window: Long) : DeviceHandler {

  override fun getValue(event: Int): Float {
    if (glfwGetKey(window, event) == GLFW_PRESS)
      return 1f

    return 0f
  }

  init {
    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
  }
}

private fun gatherCommands(deviceInput: DeviceInput): Commands {
  return deviceInput.bindings
      .mapValues { Pair(it.value, deviceInput.handler.getValue(it.key)) }
      .filter({ it.value.second != 0f })
      .values.map({ Command(it.first.type, it.first.target, it.second) }).toTypedArray()
}

data class InputConfiguration(
    val keyboard: BindingMap,
    val mouse: BindingMap,
    val gamepad1: BindingMap,
    val gamepad2: BindingMap
)

fun getCommands(inputRoot: InputRoot): Commands {
  return inputRoot.map({ gatherCommands(it) }).toTypedArray().flatten().toTypedArray()
}

fun createNewInputRoot(window: Long, config: InputConfiguration): InputRoot = arrayOf(
    DeviceInput("keyboard", KeyboardDeviceHandler(window), config.keyboard)
)