package generation.architecture.blocks

import generation.architecture.building.ledgeSlopeBuilder
import generation.architecture.connecting.Sides
import generation.architecture.connecting.levelConnectors
import generation.architecture.connecting.levelSides
import generation.architecture.engine.Builder
import generation.architecture.engine.getTurnDirection
import generation.architecture.matrical.*
import generation.architecture.matrical.sides
import generation.general.*
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute
import simulation.misc.cellLength
import simulation.misc.floorOffset

fun slopeSides(lower: Int, upperSide: Side) =
    sides(
        east = upperSide,
        west = levelSides[lower].open,
        north = levelSides[lower].slopeSides[0],
        south = levelSides[lower].slopeSides[1]
    )

fun newLedgeSide(level: Int) =
    Side(levelConnectors[level].doorway, setOf(levelConnectors[level].open), connectionLogic = ConnectionLogic.required)

fun ledgeSlopeBlock(ledgeTurns: Int): TieredBlock = { level ->
  val lower = level
  if (level == 3)
    null
  else {
    val upper = getWrappingLevelIndex(lower + 1)
    val upperSides = levelSides[upper]
    val lowerSides = levelSides[lower]
    val height = getLevelHeight(lower)
    Block(
        name = "LedgeSlope-$ledgeTurns-$level",
        sides = sides(
            east = newLedgeSide(upper),
            west = levelSides[lower].doorway,
            north = lowerSides.slopeSides[0],
            south = lowerSides.slopeSides[1]
        ) + mapOf(
            getTurnDirection(ledgeTurns) to upperSides.open
        ),
        slots = listOf(
            Vector3(cellLength * 0.25f, cellLength * (0.5f + ledgeTurns.toFloat() * 0.25f), height - quarterStep)
        ),
        attributes = setOf(CellAttribute.traversable)
    )
  }
}

val slopeWrap = Block(
    name = "octaveWrapSpace",
    sides = sides(
        east = levelSides[0].openRequired,
        down = Sides.slopeOctaveWrap
    ),
    attributes = setOf(CellAttribute.traversable)
)

val fullSlope: TieredBlock = { level ->
  val lower = level
  val upper = getWrappingLevelIndex(level + 1)
  val slotMod = if (level == levelCount - 1)
    mapOf(Direction.up to Sides.slopeOctaveWrap)
  else
    mapOf()

  Block(
      name = "slope$level",
      sides = slopeSides(lower, levelSides[upper].open)
          .plus(slotMod),
      attributes = setOf(CellAttribute.traversable),
      slots = listOf(Vector3(0f, 0f, getLevelHeight(lower) + quarterStep / 2f + 0.05f) + floorOffset)
  )
}

fun ledgeSlope(texture: TextureName): List<PartiallyTieredBlockBuilder> = listOf(
    ledgeSlopeBlock(-1) to ledgeSlopeBuilder(texture, -1),
    ledgeSlopeBlock(1) to ledgeSlopeBuilder(texture, 1)
)

val cornerSlope: TieredBlock = { level ->
  val lowerSides = levelSides[level]
  Block(
      name = "cornerSlope$level",
      sides = sides(
          east = lowerSides.open,
          south = lowerSides.open,
          west = lowerSides.slopeSides[0],
          north = lowerSides.slopeSides[1]
      ),
      attributes = setOf(CellAttribute.traversable)
  )
}
