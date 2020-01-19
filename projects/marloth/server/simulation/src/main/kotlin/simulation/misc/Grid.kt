package simulation.misc

import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3

const val cellLength = 10f

val cellHalfLength = cellLength / 2f

val floorOffset = Vector3(cellHalfLength, cellHalfLength, 0f)
val cellCenterOffset = Vector3(cellHalfLength)

data class Cell(
    val attributes: Set<CellAttribute> = setOf()
)

fun absoluteCellPosition(position: Vector3i): Vector3 =
    position.toVector3() * cellLength

typealias ConnectionPair = Pair<Vector3i, Vector3i>

typealias CellMap = Map<Vector3i, Cell>
typealias ConnectionSet = Set<ConnectionPair>

data class MapGrid(
    val cells: CellMap = mapOf(),
    val connections: ConnectionSet = setOf()
)

fun containsConnection(connections: ConnectionSet, first: Vector3i, second: Vector3i): Boolean =
    connections.contains(Pair(first, second)) || connections.contains(Pair(second, first))

fun containsConnection(position: Vector3i): (ConnectionPair) -> Boolean = { connection ->
  connection.first == position || connection.second == position
}

fun cellConnections(connections: ConnectionSet, position: Vector3i): List<ConnectionPair> =
    connections.filter(containsConnection(position))

fun cellNeighbors(connections: ConnectionSet, position: Vector3i): List<Vector3i> =
    connections.filter(containsConnection(position))
        .map { connection -> connection.toList().first { it != position } }

fun getPointCell(point: Vector3): Vector3i =
    Vector3i(
        (point.x / cellLength).toInt(),
        (point.y / cellLength).toInt(),
        (point.z / cellLength).toInt()
    )
