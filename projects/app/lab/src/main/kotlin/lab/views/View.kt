package lab.views

import lab.LabState
import silentorb.mythic.haft.CommandHandler
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2

data class LabClientResult(
    val commands: HaftCommands,
    val state: LabState
)

typealias LabCommandMap = Map<Any, CommandHandler>

data class LabCommandState(
    val commands: List<Any>,
    val mousePosition: Vector2,
    val mouseOffset: Vector2
)

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
