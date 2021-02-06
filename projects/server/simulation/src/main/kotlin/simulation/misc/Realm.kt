package simulation.misc

import silentorb.mythic.spatial.Vector3i
import simulation.main.Deck

typealias CellBiomeMap = Map<Vector3i, BiomeName>

data class Realm(
    val deck: Deck,
)
