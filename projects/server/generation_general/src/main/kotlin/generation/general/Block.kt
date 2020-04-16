package generation.general

import simulation.misc.CellAttribute

typealias Sides = Map<Direction, Side>

data class Block(
    val sides: Sides,
    val attributes: Set<CellAttribute>
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
