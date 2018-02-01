package mythic.platforming

import haft.Gamepad
import haft.MultiDeviceScalarInputSource
import haft.ScalarInputSource
import org.joml.Vector2i

data class WindowInfo(val dimensions: Vector2i)

interface PlatformInput {
  val KeyboardInputSource: ScalarInputSource
  val GamepadInputSource: MultiDeviceScalarInputSource
  fun getGamepads(): List<Gamepad>
}

interface Display {
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