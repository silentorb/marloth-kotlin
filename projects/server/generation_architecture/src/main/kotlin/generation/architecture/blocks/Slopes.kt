package generation.architecture.blocks

import generation.architecture.building.*
import generation.architecture.definition.Sides
import generation.architecture.definition.levelConnectors
import generation.architecture.definition.levelSides
import generation.architecture.engine.getTurnDirection
import generation.architecture.matrical.*
import generation.architecture.matrical.sides
import generation.general.*
import marloth.scenery.enums.MeshId
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
    newSide(levelConnectors[level].doorway, setOf(levelConnectors[level].open), ConnectionLogic.required)

fun ledgeSlope(level: Int, name: String, ledgeTurns: Int): BiomedBlockBuilder {
  val lower = getLowerLevelIndex(level)
  val upperSides = levelSides[level]
  val lowerSides = levelSides[lower]
  val height = getLevelHeight(lower)
  return BiomedBlockBuilder(
      block = Block(
          name = name,
          sides = sides(
              east = newLedgeSide(level),
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
      ),
      builder = mergeBuilders(
          newSlopedFloorMesh(MeshId.quarterSlope, height),
          newSlopeEdgeBlock(MeshId.largeBrick, height + quarterStep + quarterStep, ledgeTurns),
          tieredWalls(lower)
      )
  )
}

fun slopeWrapping() = listOf(
    BiomedBlockBuilder(
        block = Block(
            name = "octaveWrapSlope",
            sides = slopeSides(3, levelSides[0].open)
                .plus(Direction.up to Sides.slopeOctaveWrap),
            attributes = setOf(CellAttribute.traversable)
        ),
        builder = slopeBuilder(3)
    ),
    BiomedBlockBuilder(
        block = Block(
            name = "octaveWrapSpace",
            sides = sides(
                east = levelSides[0].openRequired,
                down = Sides.slopeOctaveWrap
            ),
            attributes = setOf(CellAttribute.traversable)
        ),
        builder = cubeWallsWithFeatures(
            listOf(WallFeature.window, WallFeature.lamp, WallFeature.none), lampOffset = Vector3(0f, 0f, getLevelHeight(0) - 1.2f),
            possibleDirections = setOf(Direction.north, Direction.south)
        )
    )
)

val fullSlope: MatrixBlockBuilder = { input ->
  val level = input.level
  val lower = getLowerLevelIndex(level)
  if (level == 0)
    slopeWrapping()
  else
    listOf(
        BiomedBlockBuilder(
            block = Block(
                name = "slope$level",
                sides = slopeSides(lower, levelSides[level].open),
                attributes = setOf(CellAttribute.traversable),
                slots = listOf(Vector3(0f, 0f, getLevelHeight(lower) + quarterStep / 2f + 0.05f) + floorOffset)
            ),
            builder = slopeBuilder(lower)
        )
    )
}

val ledgeSlope: MatrixBlockBuilder = { input ->
  val level = input.level
  if (level == 0)
    listOf()
  else
    listOf(
        ledgeSlope(level, "LedgeSlopeA$level", -1),
        ledgeSlope(level, "LedgeSlopeB$level", 1)
    )
}
