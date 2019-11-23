package generation.architecture.definition

import generation.architecture.building.*
import generation.general.Block
import generation.general.Side
import generation.architecture.misc.enumerateMembers
import generation.architecture.misc.Builder
import scenery.enums.MeshId
import simulation.misc.NodeAttribute

val any: Side = setOf()
val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val impassableEmpty = setOf(ConnectionType.impassableEmpty)
val impassableHorizontalSolid: Side = setOf(ConnectionType.plainWall)
val impassableHorizontal: Side = impassableHorizontalSolid.plus(setOf(ConnectionType.decoratedWall))
val optionalOpen: Side = requiredOpen.plus(impassableHorizontal)
val optionalOpenSolid: Side = setOf(ConnectionType.open, ConnectionType.plainWall)
val impassableVertical: Side = setOf(ConnectionType.plainWall)
val spiralStaircaseBottom: Side = setOf(ConnectionType.spiralStaircaseBottom)
val spiralStaircaseTop: Side = setOf(ConnectionType.spiralStaircaseTop)
val spiralStaircaseTopOrBottom: Side = setOf(ConnectionType.spiralStaircaseBottom, ConnectionType.spiralStaircaseTop)
val extraHeadroom: Side = setOf(ConnectionType.extraHeadroom)
val verticalDiagonalAdapter = setOf(ConnectionType.verticalDiagonalAdapter1)

class BlockDefinitions {
  companion object {

    val singleCellRoom = compose(
        blockBuilder(
            up = impassableVertical,
            east = optionalOpen,
            north = optionalOpen,
            west = optionalOpen,
            south = optionalOpen,
            attributes = setOf(NodeAttribute.categoryCommon)
        ),
        floorMesh(MeshId.squareFloor.name),
        cubeWalls()
    )

    val stairBottom = compose(
        blockBuilder(
            up = spiralStaircaseTopOrBottom,
            east = optionalOpenSolid,
            north = optionalOpenSolid,
            south = optionalOpenSolid,
            west = optionalOpen,
            attributes = setOf(NodeAttribute.lockedRotation)
        ),
        floorMesh(MeshId.squareFloor.name),
        cubeWalls(),
        curvedStaircases
    )

    val stairMiddle = compose(
        blockBuilder(
            up = spiralStaircaseTop,
            down = spiralStaircaseBottom,
            east = impassableHorizontal,
            north = impassableHorizontal,
            south = impassableHorizontal,
            west = impassableHorizontal,
            attributes = setOf(NodeAttribute.lockedRotation)
        ),
        cubeWalls(),
        curvedStaircases
    )

    val stairTop = compose(
        blockBuilder(
            up = impassableVertical,
            down = spiralStaircaseTopOrBottom,
            east = optionalOpen,
            north = optionalOpen,
            south = optionalOpen,
            west = impassableHorizontal,
            attributes = setOf(NodeAttribute.lockedRotation)
        ),
        cubeWalls(),
        halfFloorMesh(MeshId.halfSquareFloor.name)
    )

    val diagonalCorner = diagonalCornerFloor(requiredOpen, 0f)

    val verticalDiagonal = compose(
        blockBuilder(
            up = impassableVertical,
            down = verticalDiagonalAdapter,
            east = requiredOpen,
            north = impassableEmpty,
            west = impassableEmpty,
            south = impassableEmpty
        )
    )
  }
}

fun enumerateBlockBuilders() =
    enumerateMembers<BlockBuilder>(BlockDefinitions)
        .plus(heights())

fun splitBlockBuilders(blockBuilders: List<BlockBuilder>): Pair<Set<Block>, Map<Block, Builder>> =
    Pair(
        blockBuilders.map { it.block }.toSet(),
        blockBuilders.associate { Pair(it.block, it.builder) }
    )

fun devFilterBlockBuilders(blockBuilders: List<BlockBuilder>): List<BlockBuilder> {
  val filter = when (System.getenv("BLOCK_FILTER")) {
    "diagonal" -> setOf(NodeAttribute.categoryCommon, NodeAttribute.categoryDiagonal)
    else -> null
  }

  return if (filter != null)
    blockBuilders.filter { it.block.attributes.intersect(filter).any() }
  else
    blockBuilders
}

fun blockBuilders(): Pair<Set<Block>, Map<Block, Builder>> =
    splitBlockBuilders(devFilterBlockBuilders(enumerateBlockBuilders()))
