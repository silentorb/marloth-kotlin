package lab.views

import org.joml.Vector2i

class GameView : View {
  override fun createLayout(dimensions: Vector2i): LabLayout {
    return LabLayout(listOf())
  }

  override fun updateState(layout: LabLayout, input: InputState, delta: Float) {

  }

  override fun getCommands(): LabCommandMap = mapOf()
}
