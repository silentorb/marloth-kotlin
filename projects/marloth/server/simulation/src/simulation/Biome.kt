package simulation

import scenery.Textures

//typealias TextureSelector = (face: FlexibleFace) -> Textures?

data class Biome(
    val name: String,
    val hasEnclosedRooms: Boolean = true,
    val floorTexture: Textures,
    val ceilingTexture: Textures? = null,
    val wallTexture: Textures? = null
)

fun createBiomes(): List<Biome> = listOf(
    Biome("checkers",
        floorTexture = Textures.checkers,
        ceilingTexture = Textures.checkers,
        wallTexture = Textures.darkCheckers
    ),
    Biome("forest",
        hasEnclosedRooms = false,
        floorTexture = Textures.grass,
        ceilingTexture = Textures.ground,
        wallTexture = Textures.ground
    )
)