package generation.architecture.blocks

import generation.architecture.building.newSlopeEdgeBlock
import generation.architecture.building.newSlopedFloorMesh
import generation.architecture.building.slopeBuilder
import generation.architecture.definition.levelConnectors
import generation.architecture.definition.levelLedgeConnectors
import generation.architecture.engine.BlockBuilder
import generation.architecture.engine.mergeBuilders
import generation.architecture.engine.sides
import generation.architecture.matrical.*
import generation.architecture.misc.getTurnDirection
import generation.general.Block
import generation.general.ConnectionLogic
import generation.general.Side
import generation.general.newSide
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute
import simulation.misc.cellLength
import simulation.misc.floorOffset

data class SlopeSideConnector(
    val level: Int,
    val alternation: Boolean
)

fun newSlopeSide(level: Int, alternation: Boolean) =
    Side(
        SlopeSideConnector(level, alternation),
        setOf(SlopeSideConnector(level, !alternation))
    )

fun slopeSides(lower: Level, upper: Level) =
    sides(
        up = upper.up,
        east = upper.side,
        west = lower.side,
        north = newSlopeSide(lower.index, true),
        south = newSlopeSide(lower.index, false)
    )

fun newLedgeSide(level: Int) =
    newSide(levelLedgeConnectors[level], setOf(levelConnectors[level]), ConnectionLogic.connectWhenPossible)

fun newLedgeSlope(lower: Level, upper: Level, name: String, ledgeTurns: Int): BlockBuilder {
  val height = lower.height + quarterStep + quarterStep
  return BlockBuilder(
      block = Block(
          name = name,
          sides = sides(
              up = upper.up,
              east = newLedgeSide(upper.index),
              west = newLedgeSide(lower.index),
              north = newSlopeSide(lower.index, true),
              south = newSlopeSide(lower.index, false)
          ) + mapOf(
              getTurnDirection(ledgeTurns) to upper.side
          ),
          slots = listOf(
              Vector3(cellLength * 0.25f, cellLength * (0.5f + ledgeTurns.toFloat() * 0.25f), height - quarterStep)
          ),
          attributes = setOf(CellAttribute.traversable)
      ),
      builder = mergeBuilders(
          newSlopedFloorMesh(MeshId.quarterSlope, lower.height),
          newSlopeEdgeBlock(MeshId.largeBrick, lower.height + quarterStep + quarterStep, ledgeTurns),
          tieredWalls(lower)
      )
  )
}

val fullSlope: MatrixBlockBuilder = { input ->
  val upper = input.level
  val lower = getLowerLevel(upper)
  val levelIndex = upper.index
  listOf(
      BlockBuilder(
          block = Block(
              name = "halfStepSlopeA$levelIndex",
              sides = slopeSides(lower, upper),
              attributes = setOf(CellAttribute.traversable),
              slots = listOf(Vector3(0f, 0f, lower.height + quarterStep / 2f + 0.05f) + floorOffset)
          ),
          builder = slopeBuilder(lower)
      )
  )
}

val ledgeSlope: MatrixBlockBuilder = { input ->
  val upper = input.level
  val lower = getLowerLevel(upper)
  val levelIndex = upper.index
  listOf(
      newLedgeSlope(lower, upper, "LedgeSlopeA$levelIndex", -1),
      newLedgeSlope(lower, upper, "LedgeSlopeB$levelIndex", 1)
  )
}
