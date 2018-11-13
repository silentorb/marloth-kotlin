package mythic.platforming

import haft.Gamepad
import haft.MultiDeviceScalarInputSource
import haft.ScalarInputSource
import mythic.spatial.Vector2
import org.joml.Vector2i

data class WindowInfo(val dimensions: Vector2i)

interface PlatformInput {
  val KeyboardInputSource: ScalarInputSource
  val GamepadInputSource: MultiDeviceScalarInputSource
  val MouseInputSource: ScalarInputSource
  fun update()
  fun getMousePosition(): Vector2
  fun getGamepads(): List<Gamepad>
  fun isMouseVisible(value: Boolean)
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