package generation.architecture.blocks

import generation.architecture.building.*
import generation.architecture.definition.BiomeId
import generation.architecture.definition.Sides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.*
import generation.general.Block
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3
import simulation.misc.CellAttribute

fun plainWallLampOffset() = Vector3(0f, 0f, -1f)

fun singleCellRoomBuilder(): BiomedBuilder =
    mergeBuilders(
        floorMesh(MeshId.squareFloor),
        cubeWallsWithFeatures(fullWallFeatures(), offset = plainWallLampOffset())
    )

fun singleCellRoom() = BiomedBlockBuilder(
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
    diagonalCorner
)

fun tieredBlocks(input: BlockMatrixInput): List<BlockBuilder> =
    commonMatrixBlocks
        .flatMap {
          it(input)
              .map(applyBiomedBlockBuilder(input.biome))
        }

fun allBlockBuilders(): List<BlockBuilder> = listOf(
    homeBlock2()
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
