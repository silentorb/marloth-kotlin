package lab.views

import commanding.CommandType
import lab.LabState
import haft.*
import lab.LabCommandType
import org.joml.Vector2i

typealias ViewInputResult = Pair<Commands<CommandType>, LabState>
typealias LabCommandMap = Map<LabCommandType, CommandHandler<LabCommandType>>

interface View {
  fun createLayout(dimensions: Vector2i): LabLayout
  fun getCommands(): LabCommandMap
}