package generation.elements

import simulation.misc.NodeAttribute

typealias Sides = Map<Direction, Side>

data class Block(
    val sides: Sides,
    val attributes: Set<NodeAttribute>
)

fun newBlock(up: Side, down: Side, east: Side, north: Side, west: Side, south: Side,
             attributes: Set<NodeAttribute> = setOf()) =
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
