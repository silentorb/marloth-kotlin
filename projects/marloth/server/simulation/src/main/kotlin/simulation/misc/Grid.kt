package simulation.misc

import mythic.spatial.Vector3i

data class Cell(
    val attributes: Set<NodeAttribute> = setOf()
)

typealias ConnectionPair = Pair<Vector3i, Vector3i>

typealias CellMap = Map<Vector3i, Cell>
typealias ConnectionMap = Set<ConnectionPair>

data class MapGrid(
    val cells: CellMap = mapOf(),
    val connections: ConnectionMap = setOf()
)

fun containsConnection(connections: ConnectionMap, first: Vector3i, second: Vector3i): Boolean =
    connections.contains(Pair(first, second)) || connections.contains(Pair(second, first))

fun containsConnection(position: Vector3i): (ConnectionPair) -> Boolean = { connection ->
  connection.first == position || connection.second == position
}

fun cellConnections(connections: ConnectionMap, position: Vector3i): List<ConnectionPair> =
    connections.filter(containsConnection(position))
