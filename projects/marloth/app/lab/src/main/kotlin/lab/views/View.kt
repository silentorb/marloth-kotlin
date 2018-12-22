package lab.views

import lab.LabState
import haft.*
import lab.LabCommandType
import marloth.clienting.GeneralCommandState
import marloth.clienting.CommandType

data class LabClientResult(
    val commands: HaftCommands<CommandType>,
    val state: LabState
)

typealias LabCommandMap = Map<LabCommandType, CommandHandler<LabCommandType>>

typealias LabCommandState = GeneralCommandState<LabCommandType>
//data class LabCommandState(
//    val commands: List<HaftCommand<LabCommandType>>,
//    val mousePosition: Vector2i,
//    val mouseOffset: Vector2i
//)

//interface View {
//  fun createLayout(dimensions: Vector2i): LayoutOld
//  fun getCommands(): LabCommandMap
//  fun updateState(layout: LayoutOld, input: LabCommandState, delta: Float)
//}