package lab.views

import lab.LabState
import haft.*
import GeneralCommandState

data class LabClientResult(
    val commands: HaftCommands,
    val state: LabState
)

typealias LabCommandMap = Map<Any, CommandHandler>

typealias LabCommandState = GeneralCommandState
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
