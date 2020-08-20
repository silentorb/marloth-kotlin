package marloth.clienting.rendering

import generation.architecture.engine.PolyominoMap
import silentorb.mythic.lookinglass.meshes.loading.loadJsonResource
import silentorb.mythic.lookinglass.toCamelCase

data class BlocksFile(
    val blocks: PolyominoMap
)

fun loadBlocks(): PolyominoMap {
  val source = loadJsonResource<BlocksFile>("blocks/blocks.json")
  return source.blocks.mapValues { (_, polyomino) ->
    polyomino.copy(
        elements = polyomino.elements.map { element ->
          element.copy(
              target = toCamelCase(element.target)
          )
        }
    )
  }
      .mapKeys { (name, _) -> toCamelCase(name) }
}
