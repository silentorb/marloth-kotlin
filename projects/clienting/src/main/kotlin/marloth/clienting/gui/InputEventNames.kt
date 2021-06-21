package marloth.clienting.gui

import marloth.clienting.Client
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import silentorb.mythic.haft.*
import simulation.misc.InputEventType

fun gatherInputEventTypeNames(client: Client): Map<InputEventType, String> {
  val input = client.platform.input
  return (GLFW.GLFW_KEY_SPACE..GLFW.GLFW_KEY_LAST)
      .mapNotNull { index ->
        val text = input.getKeyName(index)
        if (text != null && text.isNotEmpty())
          InputEventType(DeviceIndexes.keyboard, index) to text.capitalize()
        else
          null
      }
      .associate { it } +
      (1..25)
          .associate { index ->
            InputEventType(DeviceIndexes.keyboard, GLFW_KEY_F1 + index - 1) to "F$index"
          } +
      mapOf(
          GLFW_KEY_SPACE to "Space",
          GLFW_KEY_ESCAPE to "Esc",
          GLFW_KEY_ENTER to "Enter",
          GLFW_KEY_TAB to "Tab",
          GLFW_KEY_BACKSPACE to "Backspace",
          GLFW_KEY_INSERT to "Ins",
          GLFW_KEY_DELETE to "Del",
          GLFW_KEY_RIGHT to "Right",
          GLFW_KEY_LEFT to "Left",
          GLFW_KEY_DOWN to "Down",
          GLFW_KEY_UP to "Up",
          GLFW_KEY_PAGE_UP to "Page Up",
          GLFW_KEY_PAGE_DOWN to "Page Down",
          GLFW_KEY_HOME to "Home",
          GLFW_KEY_END to "End",
          GLFW_KEY_CAPS_LOCK to "Caps Lock",
          GLFW_KEY_SCROLL_LOCK to "Scroll Lock",
          GLFW_KEY_PRINT_SCREEN to "Print",
          GLFW_KEY_PAUSE to "Pause",
      )
          .mapKeys { InputEventType(DeviceIndexes.keyboard, it.key) } +
      mapOf(
          MouseCommands.button1 to "Left Mouse",
          MouseCommands.button2 to "Right Mouse",
          MouseCommands.button3 to "Middle Mouse",
          MouseCommands.scrollDown to "Mouse Scroll Down",
          MouseCommands.scrollUp to "Mouse Scroll Up",
      )
          .mapKeys { InputEventType(DeviceIndexes.mouse, it.key) } +
      mapOf(
          GAMEPAD_AXIS_LEFT_LEFT to "Left Stick Left",
          GAMEPAD_AXIS_LEFT_RIGHT to "Left Stick Right",
          GAMEPAD_AXIS_LEFT_UP to "Left Stick Up",
          GAMEPAD_AXIS_LEFT_DOWN to "Left Stick Down",

          GAMEPAD_AXIS_RIGHT_LEFT to "Right Stick Left",
          GAMEPAD_AXIS_RIGHT_RIGHT to "Right Stick Right",
          GAMEPAD_AXIS_RIGHT_UP to "Right Stick Up",
          GAMEPAD_AXIS_RIGHT_DOWN to "Right Stick Down",

          GAMEPAD_AXIS_TRIGGER_LEFT to "Left Trigger",
          GAMEPAD_AXIS_TRIGGER_RIGHT to "Right Trigger",

          GAMEPAD_BUTTON_A to "A",
          GAMEPAD_BUTTON_B to "B",
          GAMEPAD_BUTTON_X to "X",
          GAMEPAD_BUTTON_Y to "Y",

          GAMEPAD_BUTTON_LEFT_BUMPER to "Left Bumper",
          GAMEPAD_BUTTON_RIGHT_BUMPER to "Right Bumper",

          GAMEPAD_BUTTON_BACK to "Back",
          GAMEPAD_BUTTON_START to "Start",

          GAMEPAD_BUTTON_DPAD_UP to "DPad Up",
          GAMEPAD_BUTTON_DPAD_RIGHT to "DPad Right",
          GAMEPAD_BUTTON_DPAD_DOWN to "DPad Down",
          GAMEPAD_BUTTON_DPAD_LEFT to "DPad Left",

          GAMEPAD_BUTTON_LEFT_THUMB to "Left Thumb",
          GAMEPAD_BUTTON_RIGHT_THUMB to "Right Thumb",
      )
          .mapKeys { InputEventType(DeviceIndexes.gamepad, it.key) }
          .mapValues { "(${it.value})" }
}
