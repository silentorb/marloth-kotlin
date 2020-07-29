package generation.architecture.biomes

import generation.architecture.blocks.diagonalCorner
import generation.architecture.blocks.fullSlope
import generation.architecture.blocks.squareRoom
import generation.architecture.matrical.Blueprint

fun forestBiome(): Blueprint = listOf(
    squareRoom,
    fullSlope,
    diagonalCorner
)
