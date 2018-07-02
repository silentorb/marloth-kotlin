package mythic.platforming

import haft.Gamepad
import haft.MultiDeviceScalarInputSource
import haft.ScalarInputSource
import org.joml.Vector2i

data class WindowInfo(val dimensions: Vector2i)

interface PlatformInput {
  val KeyboardInputSource: ScalarInputSource
  val GamepadInputSource: MultiDeviceScalarInputSource
  val MouseInputSource: ScalarInputSource
  fun update()
  fun getMousePosition(): Vector2i
  fun getGamepads(): List<Gamepad>
  fun isMouseVisible(value: Boolean)
}

data class DisplayConfig(
    var width: Int = 800,
    var height: Int = 600,
    var fullscreen: Boolean = false,
    var windowedFullscreen: Boolean = false // Whether fullscreen uses windowed fullscreen
)

interface Display {
  fun initialize(config: DisplayConfig)
  fun swapBuffers()
  fun getInfo(): WindowInfo
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