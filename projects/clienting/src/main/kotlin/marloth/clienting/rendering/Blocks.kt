package marloth.clienting.rendering

import generation.architecture.engine.BlockElementsMap
import silentorb.mythic.lookinglass.meshes.loading.loadJsonResource
import silentorb.mythic.lookinglass.toCamelCase

data class BlocksFile(
    val blocks: BlockElementsMap
)

fun loadBlocks(): BlockElementsMap {
  val source = loadJsonResource<BlocksFile>("blocks/blocks.json")
  return source.blocks.mapValues { (_, elements) ->
    elements.map { element ->
      element.copy(
          target = toCamelCase(element.target)
      )
    }
  }
      .mapKeys { (name, _) -> toCamelCase(name) }
}
