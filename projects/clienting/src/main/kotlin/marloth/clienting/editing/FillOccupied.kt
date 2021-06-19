package marloth.clienting.editing

import generation.architecture.engine.getCellDirection
import generation.general.CellDirection
import generation.general.Direction
import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.filterByProperty
import silentorb.mythic.ent.scenery.getNodesWithAttribute
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.Vector3i
import simulation.misc.GameAttributes
import simulation.misc.GameProperties

fun getMissingOccupied(meshShapes: Map<Key, Shape>, graph: Graph): List<Vector3i> {
  val meshNodes = filterByProperty(graph, SceneProperties.mesh).map { it.source }
  val occupiedCells = getCellOccupancy(meshShapes, graph, meshNodes)
      .distinct()

  val currentSideCells = getNodesWithAttribute(graph, GameAttributes.blockSide)
      .mapNotNull { node -> getCellDirection(graph, node)?.cell }

  return occupiedCells - currentSideCells
}

fun fillOccupied(meshShapes: Map<Key, Shape>, graph: Graph, parent: Key): Graph {
  val cells = getMissingOccupied(meshShapes, graph)
  val additions = cells.flatMap { cell ->
    val node = "${cell.x},${cell.y},${cell.z}"
    listOf(
        Entry(node, SceneProperties.type, GameAttributes.blockSide),
        Entry(node, GameProperties.direction, CellDirection(cell, Direction.east)),
        Entry(node, SceneProperties.parent, parent),
    )
  }
  return graph + additions
}
