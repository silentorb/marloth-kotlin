package generation.architecture.blocks

import generation.architecture.definition.BiomeId
import generation.architecture.matrical.BiomedBlockBuilder
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.BlockMatrixInput
import generation.architecture.matrical.heights
import silentorb.mythic.spatial.Vector3

fun plainWallLampOffset() = Vector3(0f, 0f, -1f)

val commonMatrixBlocks = listOf(
    squareRoom,
    fullSlope,
    ledgeSlope,
    diagonalCorner
)

fun tieredBlocks(input: BlockMatrixInput): List<BiomedBlockBuilder> =
    commonMatrixBlocks
        .flatMap {
          it(input)
        }

fun allBlockBuilders(): List<BlockBuilder> =
    homeBlocks()
        .plus(biomeAdapters())
        .plus(
            listOf(
                BiomeId.checkers,
                BiomeId.forest,
                BiomeId.tealPalace,
                BiomeId.village
            )
                .flatMap(::heights)
        )
