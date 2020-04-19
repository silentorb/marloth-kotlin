package generation.architecture.definition

import generation.architecture.building.*
import generation.general.Side
import marloth.scenery.enums.MeshId
import silentorb.mythic.ent.mapEntryValue
import simulation.misc.CellAttribute

val any: Side = setOf()
val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val impassableEmpty = setOf(ConnectionType.impassableEmpty)
val impassableHorizontalSolid: Side = setOf(ConnectionType.plainWall)
val impassableHorizontal: Side = impassableHorizontalSolid.plus(setOf(ConnectionType.decoratedWall))
val optionalDoorway: Side = doorway.plus(impassableHorizontal)
val optionalOpen: Side = requiredOpen.plus(impassableHorizontal)
val impassableVertical: Side = setOf(ConnectionType.plainWall)
val spiralStaircaseBottom: Side = setOf(ConnectionType.spiralStaircaseBottom)
val spiralStaircaseTop: Side = setOf(ConnectionType.spiralStaircaseTop)
val spiralStaircaseTopOrBottom: Side = setOf(ConnectionType.spiralStaircaseBottom, ConnectionType.spiralStaircaseTop)
val extraHeadroom: Side = setOf(ConnectionType.extraHeadroom)
val verticalDiagonalAdapter = setOf(ConnectionType.verticalDiagonalAdapter1)
val impassableCylinder: Side = setOf(ConnectionType.cylinderWall)
val openOrSolidCylinder: Side = impassableCylinder.plus(setOf(ConnectionType.doorway))

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
        setOf(CellAttribute.categoryCommon, CellAttribute.fullFloor, CellAttribute.traversable, CellAttribute.home),
        blockBuilder(
            up = impassableVertical,
            east = optionalDoorway,
            north = optionalDoorway,
            west = optionalDoorway,
            south = optionalDoorway
        ),
        floorMesh(MeshId.squareFloor.name),
        cubeWallLamps(lampRate = 1f)
    )
  }
}

fun spiralStairBlocks(): Map<String, BlockBuilder> = mapOf(
    "stairBottom" to compose(
        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
        blockBuilder(
            up = spiralStaircaseTopOrBottom,
            east = openOrSolidCylinder,
            north = openOrSolidCylinder,
            south = impassableCylinder,
            west = openOrSolidCylinder
        ),
        floorMesh(MeshId.squareFloor.name),
        cylinderWalls(),
        curvedStaircases
    ),

    "stairMiddle" to compose(
        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
        blockBuilder(
            up = spiralStaircaseTop,
            down = spiralStaircaseBottom,
            east = impassableCylinder,
            north = impassableCylinder,
            south = impassableCylinder,
            west = impassableCylinder
        ),
        cylinderWalls(),
        curvedStaircases
    ),

    "stairTop" to compose(
        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
        blockBuilder(
            up = impassableVertical,
            down = spiralStaircaseTopOrBottom,
            east = openOrSolidCylinder,
            north = openOrSolidCylinder,
            south = openOrSolidCylinder,
            west = impassableCylinder
        ),
        cylinderWalls(),
        halfFloorMesh(MeshId.halfSquareFloor.name)
    )
)
    .mapValues(mapEntryValue(withCellAttributes(
        setOf(
            CellAttribute.lockedRotation,
            CellAttribute.spiralStaircase,
            CellAttribute.traversable
        )
    )))

fun mainBlocks(): Map<String, BlockBuilder> = mapOf(
    "singleCellRoom" to BlockDefinitions.singleCellRoom,
    "home" to BlockDefinitions.home,

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
    .plus(spiralStairBlocks())

fun allBlockBuilders() =
    mainBlocks()
        .plus(heights())
