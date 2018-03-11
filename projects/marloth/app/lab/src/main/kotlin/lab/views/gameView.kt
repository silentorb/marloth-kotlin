package lab.views

import haft.Command
import lab.LabCommandType
import org.joml.Vector2i

class GameView : View {
  override fun createLayout(dimensions: Vector2i): LabLayout {
    return LabLayout(listOf())
  }

  override fun handleInput(layout: LabLayout,commands: List<Command<LabCommandType>>) {

  }

  override fun getCommands(): LabCommandMap = mapOf()
}
