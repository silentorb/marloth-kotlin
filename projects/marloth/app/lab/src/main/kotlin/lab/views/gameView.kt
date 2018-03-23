package lab.views

import org.joml.Vector2i

data class GameViewConfig(
    var seed: Long = 1,
    var UseRandomSeed: Boolean = false,
    var worldLength: Float = 100f
)

class GameView : View {
  override fun createLayout(dimensions: Vector2i): LabLayout {
    return LabLayout(listOf())
  }

  override fun updateState(layout: LabLayout, input: InputState, delta: Float) {

  }

  override fun getCommands(): LabCommandMap = mapOf()
}
