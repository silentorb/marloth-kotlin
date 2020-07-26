package generation.architecture.definition

import generation.architecture.building.*
import generation.architecture.misc.Builder
import generation.architecture.misc.squareOffsets
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute
import simulation.misc.cellLength

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

fun plainWallLampOffset() = Vector3(0f, 0f, -1f)

// + cubeWallLamps(lampRate = 0.7f)

fun singleCellRoomBuilder(): Builder =
    mergeBuilders(
        floorMesh(MeshId.squareFloor),
        cubeWallsWithFeatures(fullWallFeatures(), offset = plainWallLampOffset())
    )

fun singleCellRoom() = BlockBuilder(
    block = Block(
        name = "cubeRoom",
        sides = sides(
            east = Sides.broadOpen,
            north = Sides.broadOpen,
            west = Sides.broadOpen,
            south = Sides.broadOpen
        ),
        attributes = setOf(CellAttribute.traversable),
        slots = squareOffsets(2)
    ),
    builder = singleCellRoomBuilder()
)

// + cubeWallLamps(lampRate = 0.7f)

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
    singleCellRoom(),
    homeBlock2(),
    diagonalCorner(
        name = "diagonalCorner",
        height = 0f,
        sides = sides(
            east = Sides.broadOpen,
            north = Sides.broadOpen,
            west = preferredHorizontalClosed(Connector.open),
            south = preferredHorizontalClosed(Connector.open)
        ),
        fallback = singleCellRoomBuilder()
    )
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
    .plus(
        listOf(
            BiomeId.checkers
//            BiomeId.forest,
//            BiomeId.tealPalace,
//            BiomeId.village
        )
            .flatMap(::heights)
    )
//    .plus(spiralStairBlocks())
