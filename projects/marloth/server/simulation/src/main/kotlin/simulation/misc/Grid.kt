package simulation.misc

import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i

const val cellLength = 10f

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

fun cellNeighbors(connections: ConnectionMap, position: Vector3i): List<Vector3i> =
    connections.filter(containsConnection(position))
        .map { connection -> connection.toList().first { it != position } }

fun getPointCell(point: Vector3): Vector3i =
    Vector3i(
        (point.x / cellLength).toInt(),
        (point.y / cellLength).toInt(),
        (point.z / cellLength).toInt()
    )
