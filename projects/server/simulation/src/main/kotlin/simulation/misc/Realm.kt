package simulation.misc

import silentorb.mythic.ent.Id
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i

typealias CellBiomeMap = Map<Vector3i, BiomeName>

data class Realm(
    val grid: MapGrid
)
