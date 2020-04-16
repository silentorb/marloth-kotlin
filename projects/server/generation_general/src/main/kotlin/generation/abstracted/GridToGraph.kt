package generation.abstracted

import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.toVector3
import simulation.misc.*

private const val horizontalScale = 20f
private const val verticalScale = 4f

fun gridToGraph(nextId: IdSource = newIdSource(1L)): (MapGrid) -> Pair<Graph, CellPositionMap> = { grid ->
  val positionScale = Vector3(horizontalScale, horizontalScale, verticalScale) / 2f
  val nodes = grid.cells
      .map { (position, cell) ->
        val id = nextId()
        val node = Node(
            id = id,
            position = position.toVector3() * positionScale,
            radius = 7f,
            attributes = cell.attributes
        )
        Pair(position, node)
      }.associate { it }
  val cellMap: CellPositionMap = nodes
      .map { Pair(it.value.id, it.key) }
      .associate { it }

  val connections = grid.connections
      .map { pair ->
        val direction = pair.second - pair.first
        InitialConnection(
            first = nodes[pair.first]!!.id,
            second = nodes[pair.second]!!.id,
            type = if (isVertical(direction)) ConnectionType.vertical else ConnectionType.doorway
        )
      }
  val graph = Graph(
      nodes = nodes.mapKeys { it.value.id },
      connections = connections
  )
  Pair(graph, cellMap)
}
