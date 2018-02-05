package lab.views

import commanding.CommandType
import haft.HaftInputState
import lab.LabConfig
import lab.LabState
import mythic.spatial.Vector2
import org.joml.Vector2i

class GameView : View {
  override fun createLayout(dimensions: Vector2i): LabLayout {
    return LabLayout(listOf())
  }

  override fun input(): ViewInputResult {
    return ViewInputResult(listOf(), LabState(mapOf(), HaftInputState(listOf())))
  }
}
