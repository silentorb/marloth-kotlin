package lab.views

import commanding.CommandType
import lab.LabState
import haft.*
import lab.LabCommandType
import org.joml.Vector2i

typealias ViewInputResult = Pair<Commands<CommandType>, LabState>
typealias LabCommandMap = Map<LabCommandType, CommandHandler<LabCommandType>>

data class InputState(
    val commands: List<Command<LabCommandType>>,
    val mousePosition: Vector2i,
    val mouseOffset: Vector2i
)

interface View {
  fun createLayout(dimensions: Vector2i): LabLayout
  fun getCommands(): LabCommandMap
  fun updateState(layout: LabLayout, input: InputState, delta: Float)
}