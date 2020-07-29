package generation.architecture.blocks

import generation.architecture.building.newSlopeEdgeBlock
import generation.architecture.building.newSlopedFloorMesh
import generation.architecture.building.slopeBuilder
import generation.architecture.definition.levelConnectors
import generation.architecture.definition.levelSides
import generation.architecture.engine.getTurnDirection
import generation.architecture.matrical.*
import generation.architecture.matrical.sides
import generation.general.Block
import generation.general.ConnectionLogic
import generation.general.newSide
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute
import simulation.misc.cellLength
import simulation.misc.floorOffset

fun slopeSides(lower: Level, upper: Level) =
    sides(
        east = upper.side,
        west = lower.side,
        north = levelSides[lower.index].slopeSides[0],
        south = levelSides[lower.index].slopeSides[1]
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

val fullSlope: MatrixBlockBuilder = { input ->
  val upper = input.levelOld
  val lower = getLowerLevel(upper)
  val level = input.level
  if (level == 0)
    listOf()
  else
    listOf(
        BiomedBlockBuilder(
            block = Block(
                name = "slope$level",
                sides = slopeSides(lower, upper),
                attributes = setOf(CellAttribute.traversable),
                slots = listOf(Vector3(0f, 0f, lower.height + quarterStep / 2f + 0.05f) + floorOffset)
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
