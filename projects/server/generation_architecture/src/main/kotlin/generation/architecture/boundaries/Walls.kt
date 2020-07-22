package generation.architecture.boundaries

import generation.architecture.building.WallPlacement
import generation.architecture.building.directionRotation
import generation.architecture.building.newWallInternal
import generation.architecture.definition.BiomeId
import marloth.scenery.enums.MeshAttribute
import generation.architecture.misc.MeshQuery
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import simulation.misc.BiomeName

fun selectWallBiome(dice: Dice, biomes: List<BiomeName>): BiomeName =
    if (biomes.contains(BiomeId.home.name))
      BiomeId.home.name
    else
      dice.takeOne(biomes)

//fun placeWall(meshQuery: MeshQuery, height: Float = 0f): BoundaryBuilder = { input ->
//  val direction = input.direction
//  val dice = input.general.dice
//  val position = input.position + Vector3(0f, 0f, height)
//  val angleZ = directionRotation(direction)
//  val mesh = input.general.selectMesh(meshQuery)
//  val wall = if (mesh != null) {
//    val biome = selectWallBiome(dice, input.boundary.toList().mapNotNull { input.general.cellBiomes[it] })
//    val biomeInfo = input.general.config.biomes[biome]!!
//    newWallInternal(WallPlacement(input.general.config, mesh, position, angleZ, biomeInfo))
//  } else
//    null
//
//  listOfNotNull(wall)
//}

//val plainWallBoundaryBuilder: BoundaryBuilder = placeWall(MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.plain)))
//val doorwayBoundaryBuilder: BoundaryBuilder = placeWall(MeshQuery(all = setOf(MeshAttribute.doorway)))
//val decoratedWallBoundaryBuilder: BoundaryBuilder = placeWall(MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.decorated)))
//val solidWallBoundaryBuilder: BoundaryBuilder = placeWall(MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.solid)))
