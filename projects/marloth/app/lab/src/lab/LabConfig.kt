package lab

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import haft.Bindings
import haft.InputProfile
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

fun createLabInputBindings() = InputProfile(0, createBindings(0, 0, mapOf(
    GLFW.GLFW_KEY_GRAVE_ACCENT to LabCommandType.toggleLab,
    GLFW.GLFW_KEY_1 to LabCommandType.toggleAbstractView,
    GLFW.GLFW_KEY_2 to LabCommandType.toggleStructureView,
    GLFW.GLFW_KEY_TAB to LabCommandType.cycleView
)))

enum class LabView {
  @JsonProperty("world")
  world,

  @JsonProperty("texture")
  texture
}

data class LabConfig(
    @field:JsonIgnore var view: LabView = LabView.world,
    var showAbstract: Boolean = true,
    var showStructure: Boolean = true,
    var showLab: Boolean = false,
    var width: Int = 800,
    var height: Int = 600,
    @field:JsonIgnore val input: LabInputConfig = LabInputConfig(listOf(createLabInputBindings()))
)