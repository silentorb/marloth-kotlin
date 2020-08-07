package generation.architecture.matrical

import generation.architecture.engine.Builder
import generation.general.Block
import generation.general.Side
import silentorb.mythic.spatial.Vector3
import simulation.misc.BiomeName
import simulation.misc.cellLength

const val quarterStep = cellLength / 4f

fun getLevelHeight(level: Int): Float = level.toFloat() * quarterStep

const val levelCount = 4

typealias TieredBlock = (Int) -> Block?
typealias TieredBuilder = (Int) -> Builder

typealias PartiallyTieredBlockBuilder = Pair<TieredBlock, Builder>

typealias TieredBlockBuilder = Pair<TieredBlock, TieredBuilder>
typealias TieredBlockBuilders = List<TieredBlockBuilder>

data class Blueprint(
    val even: List<BlockBuilder> = listOf(),
    val tiered: TieredBlockBuilders = listOf()
)

fun getWrappingLevelIndex(index: Int): Int =
    (index + levelCount) % levelCount

fun getLowerLevelIndex(index: Int): Int =
    getWrappingLevelIndex(index - 1)

data class BiomeConnector(
    val biome: BiomeName,
    val connector: Any
)

fun newBiomeSide(biome: BiomeName, side: Side): Side =
    side.copy(
        mine = BiomeConnector(biome, side.mine),
        other = side.other
            .map { BiomeConnector(biome, it) }
            .toSet()
    )

fun tieredBlocks(blockBuilders: TieredBlockBuilders): (Int) -> List<BlockBuilder> = { level ->
  blockBuilders
      .mapNotNull { (tieredBlock, builder) ->
        val block = tieredBlock(level)
        if (block == null)
          null
        else
          block to builder(level)
      }
}

fun tieredBlocks(blockBuilders: Blueprint): (Int) -> List<BlockBuilder> = { level ->
  tieredBlocks(blockBuilders.tiered)(level)
      .plus(blockBuilders.even)
}

fun perHeights(range: IntRange, blockBuilders: TieredBlockBuilders): List<BlockBuilder> =
    range
        .flatMap(tieredBlocks(blockBuilders))

fun perHeights(range: IntRange, biome: BiomeName, blockBuilders: Blueprint): List<BlockBuilder> =
    range
        .flatMap(tieredBlocks(blockBuilders))
        .map(applyBiomedBlockBuilder(biome))

fun applyBiomeBlockBuilders(biomeBlockBuilders: Map<BiomeName, Blueprint>): List<BlockBuilder> =
    biomeBlockBuilders
        .flatMap { (biome, blockBuilders) ->
          perHeights(0..3, biome, blockBuilders)
        }

fun applyBuilderLevels(builder: Builder): TieredBuilder = { level ->
  if (level == 0)
    builder
  else {
    { input ->
      builder(input)
          .map { hand ->
            val body = hand.body
            if (body == null)
              hand
            else {
              val offset = Vector3(0f, 0f, getLevelHeight(level))
              hand.copy(
                  body = body.copy(
                      position = body.position + offset
                  )
              )
            }
          }
    }
  }
}

val applyBlockBuilderLevels: (PartiallyTieredBlockBuilder) -> TieredBlockBuilder =
    { (block, builder) ->
      block to applyBuilderLevels(builder)
    }
