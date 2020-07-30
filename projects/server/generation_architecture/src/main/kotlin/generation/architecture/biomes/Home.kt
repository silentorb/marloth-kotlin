package generation.architecture.biomes

import generation.architecture.building.*
import generation.architecture.definition.BiomeId
import generation.architecture.definition.levelSides
import generation.architecture.definition.uniqueConnection
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.CellAttribute
import simulation.misc.cellLength

val homeSides = uniqueConnection("homeSides")

fun homeBlocks(): List<BlockBuilder> {
  val floor = Depiction(
      mesh = MeshId.squareFloor,
      texture = TextureId.redTile
  )
  val wall = Depiction(
      mesh = MeshId.squareWall,
      texture = TextureId.redTile
  )
  val window = Depiction(
      mesh = MeshId.squareWallWindow,
      texture = TextureId.redTile
  )
  val doorway = Depiction(
      mesh = MeshId.squareWallDoorway,
      texture = TextureId.redTile
  )
  return listOf(
      BlockBuilder(
          block = Block(
              name = "1",
              sides = sides(
                  east = levelSides[0].doorway,
                  north = homeSides.first
              ),
              attributes = setOf(CellAttribute.home, CellAttribute.traversable, CellAttribute.unique)
          ),
          builder = mergeBuilders(
              floorMesh(floor),
              floorMesh(floor, offset = Vector3(0f, 0f, cellLength - 1f)),
              placeCubeRoomWalls(window, setOf(Direction.west)),
              placeCubeRoomWalls(doorway, setOf(Direction.east)),
              placeCubeRoomWalls(wall, setOf(Direction.south)),
              handBuilder(cubeWallLamp(Direction.south, plainWallLampOffset()))
          )
      ),

      BlockBuilder(
          block = Block(
              name = "2",
              sides = sides(
                  south = homeSides.second
              ),
              attributes = setOf(CellAttribute.home, CellAttribute.traversable, CellAttribute.unique)
          ),
          builder = mergeBuilders(
              floorMesh(floor),
              floorMesh(floor, offset = Vector3(0f, 0f, cellLength - 1f)),
              placeCubeRoomWalls(window, setOf(Direction.north)),
              placeCubeRoomWalls(doorway, setOf(Direction.south)),
              placeCubeRoomWalls(wall, setOf(Direction.east, Direction.west)),
              handBuilder(cubeWallLamp(Direction.west, plainWallLampOffset()))
          )
      )
  )
      .map(applyBiomedBlockBuilder(BiomeId.home))
}
