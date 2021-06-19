package marloth.clienting.editing

import generation.architecture.building.directionRotation
import generation.general.CellDirection
import generation.general.Direction
import generation.general.isSimpleSideNode
import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.EditorCommands
import silentorb.mythic.editing.getNodeSelection
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.getLocalNodeTranslation
import silentorb.mythic.ent.scenery.nodeHasAttribute
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.misc.GameAttributes
import simulation.misc.GameProperties
import simulation.misc.absoluteCellPosition

fun updateSideNodeNames(editor: Editor, graph: Graph, previous: Graph): Commands {
  val selection = getNodeSelection(editor)
  return if (selection.size != 1)
    listOf()
  else {
    val sideNodes = selection
        .filter(nodeHasAttribute(graph, GameAttributes.blockSide))
        .filter(::isSimpleSideNode)

    sideNodes
        .mapNotNull { node ->
          val currentDirection = getNodeValue<CellDirection>(graph, node, GameProperties.direction)
          val previousDirection = getNodeValue<CellDirection>(previous, node, GameProperties.direction)
          if (currentDirection != previousDirection && currentDirection != null && previousDirection != null) {
            val isConnection = nodeHasProperty(graph, node, GameProperties.mine)
            val cell = currentDirection.cell
            val direction = currentDirection.direction
            val directionClause = if (isConnection)
              ",${direction}"
            else
              ""

            val name = "${cell.x},${cell.y},${cell.z}${directionClause}"
            Command(EditorCommands.renameNode, value = name)
          } else
            null
        }
  }
}

fun applyCellDirectionOffsets(graph: Graph): Graph {
  val signatureKey = "appliedCellDirectionOffsets"
  return if (graph.any { it.source == signatureKey })
    graph
  else {
    val nodeDirections = filterByProperty(graph, GameProperties.direction)
    val additions = nodeDirections
        .flatMap { entry ->
          val node = entry.source
          val (cell, direction) = entry.target as CellDirection
          listOfNotNull(
              if (direction != Direction.east) {
                val rotation = getNodeValue<Vector3>(graph, node, SceneProperties.rotation) ?: Vector3.zero
                Entry(node, SceneProperties.rotation, rotation + Vector3(0f, 0f, directionRotation(direction)))
              } else
                null,
              if (cell != Vector3i.zero) {
                val translation = getLocalNodeTranslation(graph, node)
                val k = translation + absoluteCellPosition(cell)
                Entry(node, SceneProperties.translation, translation + absoluteCellPosition(cell))
              } else
                null,
          )
        }

    val removed = graph
        .filter { entry ->
          (entry.property == SceneProperties.translation || entry.property == SceneProperties.rotation) &&
              additions.any { it.source == entry.source && it.property == entry.property }
        }

    (graph - removed) + additions + Entry(signatureKey, "", "")
  }
}
