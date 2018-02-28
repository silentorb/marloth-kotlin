package lab.views

import org.joml.Vector2i

class GameView : View {
  override fun createLayout(dimensions: Vector2i): LabLayout {
    return LabLayout(listOf())
  }

  override fun getCommands(): LabCommandMap = mapOf()
}
