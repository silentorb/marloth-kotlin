package generation.architecture.boundaries

import generation.architecture.definition.ConnectionType
import generation.architecture.misc.ArchitectureInput
import generation.general.*
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import simulation.main.Hand
import simulation.misc.*

data class BoundaryBuilderInput(
    val general: ArchitectureInput,
    val direction: Direction,
    val boundary: ConnectionPair,
    val position: Vector3,
    val connectionType: ConnectionType
)

typealias BoundaryBuilder = (BoundaryBuilderInput) -> List<Hand>

fun getBoundaries(cells: Set<Vector3i>): List<Pair<Direction, ConnectionPair>> {
  val pairDirections = setOf(Direction.east, Direction.south, Direction.down)
      .associateWith { directionVectors[it]!! }

  val halfDirections = setOf(Direction.west, Direction.north, Direction.up)
      .associateWith { directionVectors[it]!! }

  val filter = { cell: Vector3i, filter: (Vector3i) -> Boolean ->
    { (direction, offset): Map.Entry<Direction, Vector3i> ->
      val other = cell + offset
      if (filter(other))
        Pair(direction, Pair(cell, other))
      else
        null
    }
  }
  return cells.flatMap { cell ->
    pairDirections.mapNotNull(filter(cell) { true })
        .plus(halfDirections.mapNotNull(filter(cell) { !cells.contains(it) }))
  }
}

data class BoundaryPlot(
    val direction: Direction,
    val boundary: ConnectionPair,
    val builder: BoundaryBuilder,
    val connectionType: ConnectionType
)

fun getBuilder(general: ArchitectureInput,
               horizontalBuilders: Map<ConnectionType, BoundaryBuilder>,
               verticalBuilders: Map<ConnectionType, BoundaryBuilder>
): (Pair<Direction, ConnectionPair>) -> BoundaryPlot? = { (direction, boundary) ->
  val connectionTypes = getUsableCellSide(general.gridSideMap)(boundary.first)(direction)
  if (connectionTypes.none())
    null
  else {
    val connectionType = general.dice.takeOne(connectionTypes)
    val builder = if (isHorizontal(direction))
      horizontalBuilders[connectionType]
    else
      verticalBuilders[connectionType]

    if (builder == null)
      null
    else BoundaryPlot(
        direction = direction,
        boundary = boundary,
        builder = builder,
        connectionType = connectionType as ConnectionType
    )
  }
}

fun buildBoundaries(general: ArchitectureInput,
                    horizontalBuilders: Map<ConnectionType, BoundaryBuilder>,
                    verticalBuilders: Map<ConnectionType, BoundaryBuilder>): Map<ConnectionPair, List<Hand>> {
  val boundaries = getBoundaries(general.blockGrid.keys)
//      .filter { it.second.first == Vector3i.zero || it.second.second == Vector3i.zero }
  return boundaries
      .mapNotNull(getBuilder(general, horizontalBuilders, verticalBuilders))
      .map { (direction, boundary, builder, connectionType) ->
        val directionOffset = directionVectors[direction]!!.toVector3() * cellHalfLength
        val position = absoluteCellPosition(boundary.first) + floorOffset + directionOffset + Vector3(0f, 0f, cellHalfLength)
        val input = BoundaryBuilderInput(
            general = general,
            position = position,
            boundary = boundary,
            direction = direction,
            connectionType = connectionType as ConnectionType
        )
        Pair(boundary, builder(input))
      }
      .associate { it }
}
