package marloth.clienting

import commanding.CommandType
import haft.Binding
import haft.Bindings
import haft.createBindings
import org.lwjgl.glfw.GLFW.*

data class InputConfiguration(
    val bindings: Bindings<CommandType>
)

data class Configuration(
    val input: InputConfiguration
)

fun createNewKeyboardMapping() = createBindings(0, 0, mapOf(
    GLFW_KEY_W to CommandType.moveUp,
    GLFW_KEY_A to CommandType.moveLeft,
    GLFW_KEY_D to CommandType.moveRight,
    GLFW_KEY_S to CommandType.moveDown,

    GLFW_KEY_TAB to CommandType.switchView,

    GLFW_KEY_ESCAPE to CommandType.menuBack

))

fun createNewConfiguration(): Configuration = Configuration(
    InputConfiguration(
        createNewKeyboardMapping()
    )
)