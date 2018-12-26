package mythic.platforming

import mythic.spatial.Vector2
import org.joml.Vector2i

data class WindowInfo(val dimensions: Vector2i)

data class InputEvent(
    val device: Int,
    val index: Int,
    val value: Float
)
const val keyboardDeviceIndex = 0
const val mouseDeviceIndex = 1

interface PlatformInput {
//  val KeyboardInputSource: ScalarInputSource
//  val GamepadInputSource: MultiDeviceScalarInputSource
//  val MouseInputSource: ScalarInputSource
  fun update()
  fun getMousePosition(): Vector2
//  fun getGamepads(): List<Gamepad>
  fun isMouseVisible(value: Boolean)
  fun getEvents(): List<InputEvent>
}

interface PlatformDisplayConfig {
  var width: Int
  var height: Int
  var fullscreen: Boolean
  var windowedFullscreen: Boolean // Whether fullscreen uses windowed fullscreen
  var vsync: Boolean
  var multisamples: Int
}

interface Display {
  fun initialize(config: PlatformDisplayConfig)
  fun swapBuffers()
  fun getInfo(): WindowInfo
  fun hasFocus(): Boolean
}

interface PlatformProcess {
  fun close()
  fun isClosing(): Boolean
  fun pollEvents()
}

data class Platform(
    val display: Display,
    val input: PlatformInput,
    val process: PlatformProcess
)