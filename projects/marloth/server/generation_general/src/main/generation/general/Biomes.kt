package generation.general

import scenery.MeshName
import scenery.enums.TextureId
import simulation.misc.BiomeName

//enum class WallPlacement {
//  all,
//  none,
//  some
//}

enum class BiomeAttribute {
  alwaysWindow,
  alwaysLit,
  placeOnlyAtStart,
  placeOnlyAtEnd,
  wallsAll,
  wallsSome
}

enum class TextureGroup {
  ceiling,
  default,
  floor,
  wall
}

data class BiomeInfo(
    val name: String,
    val textures: Map<TextureGroup, TextureId>,
    val meshes: Set<MeshName>,
    val attributes: Set<BiomeAttribute> = setOf()
)

typealias BiomeInfoMap = Map<BiomeName, BiomeInfo>

fun biomeTexture(info: BiomeInfo, group: TextureGroup): TextureId? =
    info.textures[group] ?: info.textures[TextureGroup.default]
