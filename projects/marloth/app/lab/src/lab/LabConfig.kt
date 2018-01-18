package lab

import haft.Bindings
import haft.createBindings

import org.lwjgl.glfw.GLFW

enum class LabCommandType {
  toggleLab,
  cycleView,
  toggleAbstractView,
  toggleStructureView,
}

data class LabInputConfig(
    val bindings: Bindings<LabCommandType>
)

fun createLabInputBindings() = createBindings(0, 0, mapOf(
    GLFW.GLFW_KEY_GRAVE_ACCENT to LabCommandType.toggleLab,
    GLFW.GLFW_KEY_1 to LabCommandType.toggleAbstractView,
    GLFW.GLFW_KEY_2 to LabCommandType.toggleStructureView,
    GLFW.GLFW_KEY_TAB to LabCommandType.cycleView
))

enum class LabView {
  world,
  texture
}

data class LabConfig(
    var view: LabView = LabView.world,
    var showAbstract: Boolean = false,
    var showStructure: Boolean = true,
    var showLab: Boolean = false,
    val input: LabInputConfig = LabInputConfig(createLabInputBindings())
)