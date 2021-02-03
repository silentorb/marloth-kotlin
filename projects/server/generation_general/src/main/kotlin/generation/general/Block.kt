package generation.general

import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.misc.CellAttribute

typealias SideMap = Map<Direction, Side>
typealias OptionalSides = Map<Direction, Side?>

data class BlockCell(
    val sides: SideMap,
    val attributes: Set<CellAttribute> = setOf(),
)

data class Block(
    val name: String = "",
    val sidesOld: SideMap = mapOf(),
    val cells: Map<Vector3i, BlockCell> = mapOf(),
    val attributes: Set<CellAttribute> = setOf(),
    val lockedRotation: Boolean = false,
    val slots: List<Vector3> = listOf(),
    val turns: Int? = null
    ) {
  init {
    assert(slots.none { it.x > 5f || it.y > 5f || it.x < -5f || it.y < -5f })
  }
}

fun openingCount(block: Block): Int =
    block.sidesOld.count { it.value != endpoint }

fun newBlock(up: Side, down: Side, east: Side, north: Side, west: Side, south: Side,
             attributes: Set<CellAttribute> = setOf()) =
    Block(
        sidesOld = mapOf(
            Direction.up to up,
            Direction.down to down,
            Direction.east to east,
            Direction.north to north,
            Direction.west to west,
            Direction.south to south
        ),
        attributes = attributes
    )
