package marloth.clienting.editing

import generation.general.CellDirection
import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.EditorCommands
import silentorb.mythic.editing.getNodeSelection
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.getNodeValue
import silentorb.mythic.ent.scenery.nodeHasAttribute
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import simulation.misc.GameAttributes
import simulation.misc.MarlothProperties

fun updateSideNodeNames(editor: Editor, graph: Graph, previous: Graph): Commands {
  val selection = getNodeSelection(editor)
  return if (selection.size != 1)
    listOf()
  else {
    val sideNodes = selection.filter(nodeHasAttribute(graph, GameAttributes.blockSide))
    sideNodes
        .mapNotNull { node ->
          val currentDirection = getNodeValue<CellDirection>(graph, node, MarlothProperties.direction)
          val previousDirection = getNodeValue<CellDirection>(previous, node, MarlothProperties.direction)
          if (currentDirection != previousDirection && currentDirection != null && previousDirection != null) {
            val cell = currentDirection.cell
            val direction = currentDirection.direction
            val name = "${cell.x},${cell.y},${cell.z},${direction}"
            Command(EditorCommands.renameNode, value = name)
          } else
            null
        }
  }
}
