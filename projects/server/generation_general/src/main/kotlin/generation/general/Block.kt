package generation.general

import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

typealias Sides = Map<Direction, Side>

data class Block(
    val sides: Sides = mapOf(),
    val attributes: Set<CellAttribute> = setOf(),
    val slots: List<Vector3> = listOf(),
    val turns: Int = 0
)

fun newBlock(up: Side, down: Side, east: Side, north: Side, west: Side, south: Side,
             attributes: Set<CellAttribute> = setOf()) =
    Block(
        sides = mapOf(
            Direction.up to up,
            Direction.down to down,
            Direction.east to east,
            Direction.north to north,
            Direction.west to west,
            Direction.south to south
        ),
        attributes = attributes
    )