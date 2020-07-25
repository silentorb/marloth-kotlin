package generation.architecture.definition

import generation.architecture.building.placeCubeRoomWalls
import generation.architecture.building.*
import generation.architecture.misc.squareOffsets
import generation.general.Block
import marloth.scenery.enums.MeshId
import simulation.misc.CellAttribute

//val any: Side = setOf()
//val doorway: Side = setOf(ConnectionType.doorway)
//val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
//val requiredWideOpen: Side = setOf(ConnectionType.open)
//val impassableEmpty = setOf(ConnectionType.impassableEmpty)
//val impassableHorizontalSolid: Side = setOf(ConnectionType.plainWall)
//val impassableHorizontal: Side = impassableHorizontalSolid.plus(setOf(ConnectionType.decoratedWall))
//val optionalDoorway: Side = doorway.plus(impassableHorizontal)
//val optionalOpen: Side = requiredOpen.plus(impassableHorizontal)
//val impassableVertical: Side = setOf(ConnectionType.plainWall)
//val spiralStaircaseBottom: Side = setOf(ConnectionType.spiralStaircaseBottom)
//val spiralStaircaseTop: Side = setOf(ConnectionType.spiralStaircaseTop)
//val spiralStaircaseTopOrBottom: Side = setOf(ConnectionType.spiralStaircaseBottom, ConnectionType.spiralStaircaseTop)
//val extraHeadroom: Side = setOf(ConnectionType.extraHeadroom)
//val verticalDiagonalAdapter = setOf(ConnectionType.verticalDiagonalAdapter1)
//val impassableCylinder: Side = setOf(ConnectionType.cylinderWall)
//val openOrSolidCylinder: Side = impassableCylinder.plus(setOf(ConnectionType.doorway))

//val homeBlock = BlockBuilder(
//    block = Block(
//        sides = sides(
//            up = impassableVertical,
//            east = doorway,
//            north = impassableHorizontal,
//            west = impassableHorizontal,
//            south = impassableHorizontal
//        ),
//        attributes = setOf(CellAttribute.categoryCommon, CellAttribute.traversable, CellAttribute.home)
//    )
//) + floorMesh(MeshId.squareFloor) + cubeWallLamps(lampRate = 1f)

val homeBlock = BlockBuilder(
    block = Block(
        name = "home",
        sides = sides(
            east = Sides.open
        ),
        attributes = setOf(CellAttribute.home, CellAttribute.traversable),
        slots = squareOffsets(2)
    )
) + floorMesh(MeshId.squareFloor) + placeCubeRoomWalls(MeshId.squareWall) // + cubeWallLamps(lampRate = 0.7f)

fun singleCellRoom() = BlockBuilder(
    block = Block(
        name = "cubeRoom",
        sides = sides(
            east = Sides.open,
            north = Sides.open,
            west = Sides.open,
            south = Sides.open
        ),
        attributes = setOf(CellAttribute.categoryCommon, CellAttribute.traversable),
        slots = squareOffsets(2)
    )
) + floorMesh(MeshId.squareFloor) + placeCubeRoomWalls(MeshId.squareWall) // + cubeWallLamps(lampRate = 0.7f)

//fun spiralStairBlocks(): Map<String, BlockBuilder> = mapOf(
//    "stairBottom" to compose(
//        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
//        blockBuilder(
//            up = spiralStaircaseTopOrBottom,
//            east = impassableCylinder,
//            north = openOrSolidCylinder,
//            south = impassableCylinder,
//            west = openOrSolidCylinder
//        ),
//        floorMesh(MeshId.squareFloor),
//        cylinderWalls(),
//        curvedStaircases
//    ),
//
//    "stairMiddle" to compose(
//        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
//        blockBuilder(
//            up = spiralStaircaseTop,
//            down = spiralStaircaseBottom,
//            east = impassableCylinder,
//            north = impassableCylinder,
//            south = impassableCylinder,
//            west = impassableCylinder
//        ),
//        cylinderWalls(),
//        curvedStaircases
//    ),
//
//    "stairTop" to compose(
//        setOf(CellAttribute.lockedRotation, CellAttribute.traversable),
//        blockBuilder(
//            up = impassableVertical,
//            down = spiralStaircaseTopOrBottom,
//            east = openOrSolidCylinder,
//            north = impassableCylinder,
//            south = openOrSolidCylinder,
//            west = impassableCylinder
//        ),
//        cylinderWalls(),
//        halfFloorMesh(MeshId.halfSquareFloor)
//    )
//)
//    .mapValues(mapEntryValue(withCellAttributes(
//        setOf(
//            CellAttribute.lockedRotation,
//            CellAttribute.spiralStaircase,
//            CellAttribute.traversable
//        )
//    )))

fun allBlockBuilders(): List<BlockBuilder> = listOf(
    singleCellRoom()
//    "deadEnd" to deadEnd()
//    "home" to homeBlock,

//    "diagonalCorner" to diagonalCornerFloor(0f) + BlockBuilder(
//        block = Block(
//            sides = sides(
//                up = impassableVertical,
//                east = requiredWideOpen,
//                north = requiredWideOpen
//            )
//        )
//    ),
//
//    "verticalDiagonal" to compose(
//        setOf(CellAttribute.traversable),
//        blockBuilder(
//            up = impassableVertical,
//            down = verticalDiagonalAdapter,
//            east = requiredOpen,
//            north = impassableEmpty,
//            west = impassableEmpty,
//            south = impassableEmpty
//        )
//    )
)
//    .plus(heights())
//    .plus(spiralStairBlocks())
