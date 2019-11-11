package generation.elements

import mythic.spatial.Vector3i
import simulation.misc.MapGrid

typealias BlockGrid = Map<Vector3i, Block>

data class Workbench(
    val mapGrid: MapGrid,
    val blockGrid: BlockGrid
)
