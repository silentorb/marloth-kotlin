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
