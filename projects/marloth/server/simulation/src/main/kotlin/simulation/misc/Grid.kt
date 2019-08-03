package simulation.misc

import mythic.spatial.Vector3i

data class Cell(
    val attributes: Set<NodeAttribute> = setOf()
)

typealias ConnectionPair = Pair<Vector3i, Vector3i>

typealias CellMap = Map<Vector3i, Cell>
typealias ConnectionMap = Map<Vector3i, ConnectionPair>

data class MapGrid(
    val cells: CellMap = mapOf(),
    val connections: ConnectionMap = mapOf()
)
