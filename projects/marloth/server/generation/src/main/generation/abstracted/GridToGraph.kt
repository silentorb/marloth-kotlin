package generation.abstracted

import mythic.ent.IdSource
import mythic.spatial.Vector3
import mythic.spatial.toVector3
import simulation.misc.*

private const val horizontalScale = 20f
private const val verticalScale = 4f

fun gridToGraph(nextId: IdSource, grid: MapGrid): Pair<Graph, CellPositionMap> {
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
        InitialConnection(
            first = nodes[pair.first]!!.id,
            second = nodes[pair.second]!!.id,
            type = ConnectionType.doorway
        )
      }
  val graph = Graph(
      nodes = nodes.mapKeys { it.value.id },
      connections = connections
  )
  return Pair(graph, cellMap)
}
