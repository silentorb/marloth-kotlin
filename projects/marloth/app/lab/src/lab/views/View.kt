package lab.views

import commanding.CommandType
import lab.LabState
import haft.*
import lab.LabConfig
import org.joml.Vector2i

typealias ViewInputResult = Pair<Commands<CommandType>, LabState>

//typealias ViewRender = (dimensions: Vector2) -> LabLayout
//typealias ViewInput = () -> ViewInputResult

interface View {
  fun createLayout(dimensions: Vector2i): LabLayout
  fun input(): ViewInputResult
}