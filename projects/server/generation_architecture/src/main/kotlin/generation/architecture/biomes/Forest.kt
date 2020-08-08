package generation.architecture.biomes

import generation.architecture.blocks.*
import generation.architecture.building.*
import generation.architecture.connecting.Sides
import generation.architecture.engine.Builder
import generation.architecture.matrical.Blueprint
import generation.architecture.matrical.TieredBlock
import generation.architecture.matrical.applyBlockBuilderLevels
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import simulation.entities.Depiction

fun forestFloor(): Depiction =
    Depiction(
        mesh = MeshId.grassFloor
    )

fun dirtFloor(): Depiction =
    Depiction(
        mesh = MeshId.dirtFloor
    )

fun forestWall(): Depiction =
    Depiction(
        mesh = MeshId.dirtWall
    )

fun generalForestBuilder(): Builder =
    floorMesh(forestFloor())

fun withSolidBase(block: Block) =
    block.copy(
        sides = block.sides
            .plus(
                Direction.down to Sides.solid
//                    Direction.down to Sides.solidRequired
            )
    )

fun withSolidBase(tieredBlock: TieredBlock): TieredBlock = { level ->
  withSolidBase(tieredBlock(level)!!)
}

fun forestBiome(): Blueprint {
  val floor = forestFloor()
  val texture = TextureId.grass
  return Blueprint(
      even = listOf(
          slopeWrap to emptyBuilder,
          solidBlock() to solidCubeBuilder(forestWall(), dirtFloor()),
          solidDiagonal() to solidDiagonalBuilder(forestWall(), dirtFloor())
      ),
      tiered = listOf(
          withSolidBase(squareRoom) to floorMesh(floor),
          withSolidBase(fullSlope) to newSlopedFloorMesh(Depiction(mesh = MeshId.fullSlope, texture = texture)),
          diagonalCornerBlock to diagonalHalfFloorMesh(Depiction(mesh = MeshId.squareFloorHalfDiagonal, texture = texture)),
          withSolidBase(cornerSlope) to cornerSlope(texture)
      )
          .map(applyBlockBuilderLevels)
  )
}
