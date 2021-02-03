package generation.architecture.biomes

import generation.architecture.building.*
import generation.architecture.connecting.Sides
import generation.architecture.connecting.levelSides
import generation.architecture.connecting.uniqueConnection
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureIdOld
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.CellAttribute
import simulation.misc.cellLength

val homeSides = uniqueConnection("homeSides")

fun homeBlocks(): List<BlockBuilder> {
  val floor = Depiction(
      mesh = MeshId.floorSquare,
      texture = TextureIdOld.redTile
  )
  val wall = Depiction(
//      mesh = MeshId.dirtWall
      mesh = MeshId.wallSquareShort,
      texture = TextureIdOld.redTile
  )
  val window = Depiction(
      mesh = MeshId.squareWallWindow,
      texture = TextureIdOld.redTile
  )
  val doorway = Depiction(
      mesh = MeshId.squareWallDoorway,
      texture = TextureIdOld.redTile
  )
  return listOf(
      Block(
          name = "1",
          sidesOld = sides(
              east = levelSides[0].doorway,
              north = homeSides.first,
              up = Sides.headroomVertical
          ),
          attributes = setOf(CellAttribute.home, CellAttribute.isTraversable, CellAttribute.unique)
      ) to mergeBuilders(
          floorMesh(floor),
          floorMesh(floor, offset = Vector3(0f, 0f, cellLength - 1f)),
          placeCubeRoomWalls(window, setOf(Direction.west)),
          placeCubeRoomWalls(doorway, setOf(Direction.east)),
          placeCubeRoomWalls(wall, setOf(Direction.south)),
          handBuilder(cubeWallLamp(Direction.south, plainWallLampOffset()))
      ),

      Block(
          name = "2",
          sidesOld = sides(
              south = homeSides.second
          ),
          attributes = setOf(CellAttribute.home, CellAttribute.isTraversable, CellAttribute.unique)
      ) to mergeBuilders(
          floorMesh(floor),
          floorMesh(floor, offset = Vector3(0f, 0f, cellLength - 1f)),
          placeCubeRoomWalls(window, setOf(Direction.north)),
          placeCubeRoomWalls(doorway, setOf(Direction.south)),
          placeCubeRoomWalls(wall, setOf(Direction.east, Direction.west)),
          handBuilder(cubeWallLamp(Direction.west, plainWallLampOffset()))
      )
  )
      .map(applyBiomedBlockBuilder(BiomeId.home))
}
