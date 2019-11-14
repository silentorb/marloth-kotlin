package generation.architecture.definition

import generation.architecture.building.*
import generation.architecture.cellLength
import generation.elements.Block
import generation.elements.Side
import generation.elements.enumerateMembers
import generation.next.Builder
import mythic.spatial.Vector3
import scenery.enums.MeshId
import simulation.misc.NodeAttribute

val any: Side = setOf()
val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val impassableHorizontalSolid: Side = setOf(ConnectionType.wall)
val impassableHorizontal: Side = impassableHorizontalSolid.plus(setOf(ConnectionType.window))
val optionalOpen: Side = requiredOpen.plus(impassableHorizontal)
val optionalOpenSolid: Side = setOf(ConnectionType.open, ConnectionType.wall)
val impassableVertical: Side = setOf(ConnectionType.wall)
val spiralStaircaseBottom: Side = setOf(ConnectionType.spiralStaircaseBottom)
val spiralStaircaseTop: Side = setOf(ConnectionType.spiralStaircaseTop)
val spiralStaircaseTopOrBottom: Side = setOf(ConnectionType.spiralStaircaseBottom, ConnectionType.spiralStaircaseTop)
val halfStepRequiredOpen: Side = setOf(ConnectionType.halfStepOpen)
val halfStepOptionalOpen: Side = halfStepRequiredOpen.plus(ConnectionType.wall)

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
            north = optionalOpen,
            south = optionalOpen,
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

    val halfStepRoom = compose(
        blockBuilder(
            up = impassableVertical,
            east = halfStepOptionalOpen,
            north = halfStepOptionalOpen,
            west = halfStepOptionalOpen,
            south = halfStepOptionalOpen
        ),
        floorMesh(MeshId.squareFloor.name, Vector3(0f, 0f, cellLength / 4f)),
        cubeWalls()
    )

    val lowerHalfStepSlope = compose(
        blockBuilder(
            up = impassableVertical,
            east = halfStepRequiredOpen,
            north = impassableHorizontalSolid,
            west = requiredOpen,
            south = impassableHorizontalSolid
        ),
        floorMesh(MeshId.squareFloor.name),
        newSlopedFloorMesh(MeshId.squareFloor.name),
        cubeWalls()
    )

    val diagonalCorner = compose(
        blockBuilder(
            up = impassableVertical,
            down = impassableVertical,
            east = requiredOpen,
            north = requiredOpen,
            west = impassableHorizontal,
            south = impassableHorizontal,
            attributes = setOf(NodeAttribute.categoryDiagonal)
        ),
        diagonalHalfFloorMesh(MeshId.halfSquareFloor.name)
    )
  }
}

fun enumerateBlockBuilders() = enumerateMembers<BlockBuilder>(BlockDefinitions)

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
