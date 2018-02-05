package lab

import com.fasterxml.jackson.annotation.JsonIgnore
import haft.InputProfiles
import haft.createBindings
import org.lwjgl.glfw.GLFW

enum class LabCommandType {
  toggleLab,
  cycleView,
  toggleAbstractView,
  toggleStructureView,
}

data class LabInputConfig(
    val profiles: InputProfiles<LabCommandType>
)

fun createLabInputBindings() = createBindings(0, 0, mapOf(
    GLFW.GLFW_KEY_GRAVE_ACCENT to LabCommandType.toggleLab,
    GLFW.GLFW_KEY_1 to LabCommandType.toggleAbstractView,
    GLFW.GLFW_KEY_2 to LabCommandType.toggleStructureView,
    GLFW.GLFW_KEY_TAB to LabCommandType.cycleView
))

//enum class LabView {
//  game,
//  model,
//  world,
//  texture
//}

data class WorldViewConfig(
    var showAbstract: Boolean = true,
    var showStructure: Boolean = true
)

data class ModelViewConfig(
    var model: String = "cube",
    var drawVertices: Boolean = true,
    var drawEdges: Boolean = true
)

data class LabConfig(
    var view: String = "world",
    var worldView: WorldViewConfig = WorldViewConfig(),
    var modelView: ModelViewConfig = ModelViewConfig(),
    var width: Int = 800,
    var height: Int = 600,
    @field:JsonIgnore val input: LabInputConfig = LabInputConfig(listOf(createLabInputBindings()))
)