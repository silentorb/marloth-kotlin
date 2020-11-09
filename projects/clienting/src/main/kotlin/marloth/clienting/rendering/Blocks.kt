package marloth.clienting.rendering

import generation.architecture.engine.PolyominoMap
import silentorb.mythic.spatial.serialization.loadSpatialJsonResource
import silentorb.mythic.lookinglass.toCamelCase

data class BlocksFile(
    val blocks: PolyominoMap
)

fun loadBlocks(): PolyominoMap {
  val source = loadSpatialJsonResource<BlocksFile>("blocks/blocks.json")
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
