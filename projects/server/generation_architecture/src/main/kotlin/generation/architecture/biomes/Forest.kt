package generation.architecture.biomes

import generation.architecture.blocks.*
import generation.architecture.building.*
import generation.architecture.engine.Builder
import generation.architecture.matrical.Blueprint
import generation.architecture.matrical.applyBlockBuilderLevels
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import simulation.entities.Depiction

fun forestFloor(): Depiction =
    Depiction(
        mesh = MeshId.grassFloor
    )

fun forestWall(): Depiction =
    Depiction(
        mesh = MeshId.dirtWall
    )

fun generalForestBuilder(): Builder =
    floorMesh(forestFloor())

fun forestBiome(): Blueprint {
  val floor = forestFloor()
  val texture = TextureId.grass
  return Blueprint(
      even = listOf(
          slopeWrap(emptyBuilder)
      ),
      tiered = listOf(
          squareRoom to floorMesh(floor),
          fullSlope to newSlopedFloorMesh(Depiction(mesh = MeshId.quarterSlope, texture = texture)),
          diagonalCornerBlock to diagonalHalfFloorMesh(Depiction(mesh = MeshId.squareFloorHalfDiagonal, texture = texture)),
          cornerSlope to cornerSlope(texture)
      )
          .map(applyBlockBuilderLevels)
  )
}
