package generation.architecture.definition

import generation.architecture.building.*
import generation.general.Block
import generation.general.Side
import generation.architecture.misc.Builder
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute

val any: Side = setOf()
val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val impassableEmpty = setOf(ConnectionType.impassableEmpty)
val impassableHorizontalSolid: Side = setOf(ConnectionType.plainWall)
val impassableHorizontal: Side = impassableHorizontalSolid.plus(setOf(ConnectionType.decoratedWall))
val optionalDoorway: Side = doorway.plus(impassableHorizontal)
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
        setOf(CellAttribute.categoryCommon, CellAttribute.fullFloor, CellAttribute.traversable),
        blockBuilder(
            up = impassableVertical,
            east = optionalOpen,
            north = optionalOpen,
            west = optionalOpen,
            south = optionalOpen
        ),
        floorMesh(MeshId.squareFloor.name),
        cubeWallLamps(lampRate = 0.7f)
    )

    val home = compose(
        setOf(CellAttribute.categoryCommon, CellAttribute.fullFloor, CellAttribute.traversable),
        blockBuilder(
            up = impassableVertical,
            east = optionalDoorway,
            north = optionalDoorway,
            west = optionalDoorway,
            south = optionalDoorway
        ),
        floorMesh(MeshId.squareFloor.name),
        cubeWallLamps(lampRate = 0.7f)
    )
  }
}

fun mainBlocks(): Map<String, BlockBuilder> = mapOf(
    "singleCellRoom" to BlockDefinitions.singleCellRoom,
    "home" to BlockDefinitions.home,
    "stairBottom" to compose(
        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
        blockBuilder(
            up = spiralStaircaseTopOrBottom,
            east = optionalOpenSolid,
            north = optionalOpenSolid,
            south = optionalOpenSolid,
            west = optionalOpen
        ),
        floorMesh(MeshId.squareFloor.name),
        cubeWalls(),
        curvedStaircases
    ),

    "stairMiddle" to compose(
        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
        blockBuilder(
            up = spiralStaircaseTop,
            down = spiralStaircaseBottom,
            east = impassableHorizontal,
            north = impassableHorizontal,
            south = impassableHorizontal,
            west = impassableHorizontal
        ),
        cubeWalls(),
        curvedStaircases
    ),

    "stairTop" to compose(
        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
        blockBuilder(
            up = impassableVertical,
            down = spiralStaircaseTopOrBottom,
            east = optionalOpen,
            north = optionalOpen,
            south = optionalOpen,
            west = impassableHorizontal
        ),
        cubeWalls(),
        halfFloorMesh(MeshId.halfSquareFloor.name)
    ),

    "diagonalCorner" to diagonalCornerFloor(requiredOpen, 0f),

    "verticalDiagonal" to compose(
        setOf(CellAttribute.traversable),
        blockBuilder(
            up = impassableVertical,
            down = verticalDiagonalAdapter,
            east = requiredOpen,
            north = impassableEmpty,
            west = impassableEmpty,
            south = impassableEmpty
        )
    )
)

fun enumerateBlockBuilders() =
    mainBlocks()
        .plus(heights())

fun splitBlockBuilders(blockBuilders: Collection<BlockBuilder>): Pair<Set<Block>, Map<Block, Builder>> =
    Pair(
        blockBuilders.map { it.block }.toSet(),
        blockBuilders.associate { Pair(it.block, it.builder) }
    )

fun devFilterBlockBuilders(blockBuilders: Collection<BlockBuilder>): Collection<BlockBuilder> {
  val filter = when (System.getenv("BLOCK_FILTER")) {
    "diagonal" -> setOf(CellAttribute.categoryCommon, CellAttribute.categoryDiagonal)
    else -> null
  }

  return if (filter != null)
    blockBuilders.filter { it.block.attributes.intersect(filter).any() }
  else
    blockBuilders
}
