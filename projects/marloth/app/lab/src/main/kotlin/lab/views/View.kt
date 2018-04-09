package lab.views

import commanding.CommandType
import lab.LabState
import haft.*
import lab.LabCommandType
import marloth.clienting.gui.MenuActionType
import mythic.bloom.Layout
import org.joml.Vector2i

data class LabClientResult(
    val commands: Commands<CommandType>,
    val state: LabState,
    val menuAction: MenuActionType
)

typealias LabCommandMap = Map<LabCommandType, CommandHandler<LabCommandType>>

data class InputState(
    val commands: List<Command<LabCommandType>>,
    val mousePosition: Vector2i,
    val mouseOffset: Vector2i
)

interface View {
  fun createLayout(dimensions: Vector2i): Layout
  fun getCommands(): LabCommandMap
  fun updateState(layout: Layout, input: InputState, delta: Float)
}