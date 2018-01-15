package marloth.clienting

import commanding.CommandType
import haft.Binding
import haft.Bindings
import org.lwjgl.glfw.GLFW.*

data class InputConfiguration(
    val bindings: Bindings
)

data class Configuration(
    val input: InputConfiguration
)

fun createBindings(device: Int, target: Int, bindings: Map<Int, CommandType>): Bindings =
    bindings.map({ Binding(device, it.key, it.value, target) })

fun createNewKeyboardMapping(): Bindings = createBindings(0, 0, mapOf(
    GLFW_KEY_W to CommandType.moveUp,
    GLFW_KEY_A to CommandType.moveLeft,
    GLFW_KEY_D to CommandType.moveRight,
    GLFW_KEY_S to CommandType.moveDown,

    GLFW_KEY_TAB to CommandType.switchView,

    GLFW_KEY_GRAVE_ACCENT to CommandType.toggleLab,
    GLFW_KEY_1 to CommandType.toggleAbstractView,
    GLFW_KEY_2 to CommandType.toggleStructureView,

    GLFW_KEY_ESCAPE to CommandType.menuBack

))

fun createNewConfiguration(): Configuration = Configuration(
    InputConfiguration(
        createNewKeyboardMapping()
    )
)