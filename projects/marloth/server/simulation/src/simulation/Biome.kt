package simulation

import scenery.Textures

//typealias TextureSelector = (face: FlexibleFace) -> Textures?

enum class Enclosure {
  all,
  none,
  some
}
data class Biome(
    val name: String,
    val enclosure: Enclosure,
    val floorTexture: Textures,
    val ceilingTexture: Textures? = null,
    val wallTexture: Textures? = null
)

fun createBiomes(): List<Biome> = listOf(
    Biome("checkers",
        enclosure = Enclosure.some,
        floorTexture = Textures.checkers,
        ceilingTexture = Textures.checkers,
        wallTexture = Textures.darkCheckers
    ),
    Biome("forest",
        enclosure = Enclosure.none,
        floorTexture = Textures.grass,
        ceilingTexture = Textures.ground,
        wallTexture = Textures.ground
    ),
    Biome("home",
        enclosure = Enclosure.all,
        floorTexture = Textures.red_tile02,
        ceilingTexture = Textures.red_tile01,
        wallTexture = Textures.red_tile01
    )
)