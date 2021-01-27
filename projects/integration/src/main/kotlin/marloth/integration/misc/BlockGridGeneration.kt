package marloth.integration.misc

import generation.architecture.biomes.allBlockBuilders
import generation.architecture.engine.*
import generation.architecture.matrical.BlockBuilder
import generation.general.BlockGrid
import generation.general.rotateSides
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import simulation.main.Hand
import simulation.misc.CellAttribute

fun explodeBlockMap(blockBuilders: Collection<BlockBuilder>): List<BlockBuilder> {
  assert(blockBuilders.all { it.first.name.isNotEmpty() })
  val (needsRotatedVariations, noTurns) = blockBuilders
      .partition { (block, _) ->
        !block.attributes.contains(CellAttribute.lockedRotation) &&
            block.sides != rotateSides(1)(block.sides)
      }

  val rotated = needsRotatedVariations
      .flatMap { (block, builder) ->
        (0..3)
            .map { turns ->
              val rotation = Quaternion().rotateZ(applyTurns(turns))
              block.copy(
                  sides = rotateSides(turns)(block.sides),
                  slots = block.slots.map { slot ->
                    rotation.transform(slot)
                  },
                  turns = turns
              ) to builder
            }
      }

  return noTurns + rotated
}

fun generateWorldBlocks(dice: Dice, generationConfig: GenerationConfig): Pair<BlockGrid, List<Hand>> {
  val importedBlockBuilders = generationConfig.polyominoes
      .flatMap { (name, polyomino) ->
        blockBuildersFromElements(name, polyomino)
      }

  val blockBuilders = explodeBlockMap(allBlockBuilders() + importedBlockBuilders)
//  val blockBuilders = explodeBlockMap(allBlockBuilders())
  val (blocks, builders) = splitBlockBuilders(devFilterBlockBuilders(blockBuilders))
//  val home = blocks.first { it.name == "home" }
  val home = blocks.first { it.name == "home-1" }
  val blockGrid = newBlockGrid(dice, home, blocks - home, generationConfig.roomCount)
  val architectureInput = newArchitectureInput(generationConfig, dice, blockGrid)
  val architectureSource = buildArchitecture(architectureInput, builders)
  return Pair(blockGrid, architectureSource)
}
