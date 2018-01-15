package mythic.platforming
import org.joml.Vector2i

data class WindowInfo(val dimensions: Vector2i)

interface Input {

  fun isKeyPressed(key: Int): Boolean
}

interface Display {
  //  fun createWindow()
  fun swapBuffers()

  fun getInfo(): WindowInfo
}

interface PlatformProcess {
  fun isClosing(): Boolean
  fun pollEvents()
}

data class Platform(
    val display: Display,
    val input: Input,
    val process: PlatformProcess
)