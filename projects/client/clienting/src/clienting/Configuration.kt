package clienting

import commanding.CommandType
import haft.Binding
import haft.BindingMap
import haft.InputConfiguration
import org.lwjgl.glfw.GLFW.*

data class Configuration(
    val input: InputConfiguration
)

fun createNewKeyboardMapping(): BindingMap = mapOf(
    GLFW_KEY_W to CommandType.moveUp,
    GLFW_KEY_A to CommandType.moveLeft,
    GLFW_KEY_D to CommandType.moveRight,
    GLFW_KEY_S to CommandType.moveDown,

    GLFW_KEY_ESCAPE to CommandType.menuBack

).mapValues({ Binding(it.value, 0) })

fun createNewConfiguration(): Configuration = Configuration(
    InputConfiguration(
        createNewKeyboardMapping(),
        mapOf(),
        mapOf(),
        mapOf()
    )
)