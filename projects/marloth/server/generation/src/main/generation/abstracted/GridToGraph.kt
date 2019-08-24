package generation.abstracted

import mythic.ent.IdSource
import mythic.spatial.Vector3
import mythic.spatial.toVector3
import simulation.misc.*

fun gridToGraph(nextId: IdSource, grid: MapGrid): Pair<Graph, CellPositionMap> {
  val horizontalScale = 30f
  val verticalScale = 10f
  val positionScale = Vector3(horizontalScale, horizontalScale, verticalScale) / 2f
  val nodes = grid.cells
      .map { (position, cell) ->
        val id = nextId()
        val node = Node(
            id = id,
            position = position.toVector3() * positionScale,
            isRoom = true,
            radius = 7f,
            attributes = cell.attributes
        )
        Pair(position, node)
      }.associate { it }
  val cellMap: CellPositionMap = nodes
      .map { Pair(it.value.id, it.key) }
      .associate { it }

  val connections = grid.connections
      .map { (_, pair) ->
        InitialConnection(
            first = nodes[pair.first]!!.id,
            second = nodes[pair.second]!!.id,
            type = ConnectionType.tunnel
        )
      }
  val graph = Graph(
      nodes = nodes.mapKeys { it.value.id },
      connections = connections
  )
  return Pair(graph, cellMap)
}
