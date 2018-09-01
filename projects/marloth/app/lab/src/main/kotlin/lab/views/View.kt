package lab.views

import lab.LabState
import haft.*
import lab.LabCommandType
import marloth.clienting.GeneralCommandState
import marloth.clienting.CommandType
import marloth.clienting.gui.MenuActionType

data class LabClientResult(
    val commands: HaftCommands<CommandType>,
    val state: LabState,
    val menuAction: MenuActionType
)

typealias LabCommandMap = Map<LabCommandType, CommandHandler<LabCommandType>>

typealias LabCommandState = GeneralCommandState<LabCommandType>
//data class LabCommandState(
//    val commands: List<HaftCommand<LabCommandType>>,
//    val mousePosition: Vector2i,
//    val mouseOffset: Vector2i
//)

//interface View {
//  fun createLayout(dimensions: Vector2i): Layout
//  fun getCommands(): LabCommandMap
//  fun updateState(layout: Layout, input: LabCommandState, delta: Float)
//}