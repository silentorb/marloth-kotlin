package generation.architecture.biomes

import generation.architecture.blocks.*
import generation.architecture.building.cubeRoomBuilder
import generation.architecture.building.diagonalCornerBuilder
import generation.architecture.building.slopeBuilder
import generation.architecture.building.slopeWrapBuilder
import generation.architecture.engine.Builder
import generation.architecture.matrical.Blueprint
import generation.architecture.matrical.applyBlockBuilderLevels
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import simulation.entities.Depiction

fun checkersFloor(): Depiction =
    Depiction(
        mesh = MeshId.floorSquare,
        texture = TextureId.checkersBlackWhite
    )

fun checkersWall(): Depiction =
    Depiction(
        mesh = MeshId.wallSquareShort,
        texture = TextureId.checkersBlackWhite
    )

fun generalCheckersBuilder(): Builder =
    cubeRoomBuilder(checkersFloor(), checkersWall())

fun checkersBiome(): Blueprint {
  val texture = TextureId.checkersBlackWhite
  val floor = checkersFloor()
  val wall = checkersWall()
  return Blueprint(
      even = listOf(
          slopeWrap to slopeWrapBuilder(wall)
      ),
      tiered = listOf(
          squareRoom to cubeRoomBuilder(floor, wall),
          fullSlope to slopeBuilder(Depiction(mesh = MeshId.fullSlope, texture = texture), wall),
          diagonalCornerBlock to diagonalCornerBuilder(texture)
      )
          .plus(ledgeSlope(texture))
          .map(applyBlockBuilderLevels)
  )
}
