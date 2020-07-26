package generation.architecture.blocks

import generation.architecture.building.*
import generation.architecture.definition.BiomeId
import generation.architecture.definition.Connector
import generation.architecture.definition.Sides
import generation.architecture.definition.preferredHorizontalClosed
import generation.architecture.engine.BlockBuilder
import generation.architecture.engine.mergeBuilders
import generation.architecture.engine.sides
import generation.architecture.matrical.BlockMatrixInput
import generation.architecture.matrical.heights
import generation.architecture.misc.Builder
import generation.architecture.misc.squareOffsets
import generation.general.Block
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

fun plainWallLampOffset() = Vector3(0f, 0f, -1f)

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

val commonMatrixBlocks = listOf(
    squareRoom,
    fullSlope,
    ledgeSlope,
    diagnoseCorner
)

fun tieredBlocks(input: BlockMatrixInput): List<BlockBuilder> =
    commonMatrixBlocks.flatMap { it(input) }

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
