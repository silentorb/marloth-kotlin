package generation.architecture.biomes

import generation.architecture.blocks.*
import generation.architecture.building.*
import generation.architecture.connecting.Sides
import generation.architecture.engine.Builder
import generation.architecture.matrical.Blueprint
import generation.architecture.matrical.TieredBlock
import generation.architecture.matrical.applyBlockBuilderLevels
import generation.architecture.matrical.mergeBuilders
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import simulation.entities.Depiction

fun grassFloor(): Depiction =
    Depiction(
        mesh = MeshId.grassFloor
    )

fun grassDiagonalFloor(): Depiction =
    Depiction(
        mesh = MeshId.grassDiagonalFloor
    )

fun dirtFloor(): Depiction =
    Depiction(
        mesh = MeshId.dirtFloor
    )

fun dirtDiagonalFloor(): Depiction =
    Depiction(
        mesh = MeshId.dirtDiagonalFloor
    )

fun forestWall(): Depiction =
    Depiction(
        mesh = MeshId.dirtWall
    )

fun treeBranching(): Depiction =
    Depiction(
        mesh = MeshId.treeBranching
    )

fun generalForestBuilder(): Builder =
    floorMesh(grassFloor())

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

fun withDiagonalSolidBase(block: Block) =
    block.copy(
        sides = block.sides
            .plus(
                Direction.down to Sides.solidDiagonalVertical
//                    Direction.down to Sides.solidRequired
            )
    )

fun withDiagonalSolidBase(tieredBlock: TieredBlock): TieredBlock = { level ->
  withDiagonalSolidBase(tieredBlock(level)!!)
}

fun forestBiome(): Blueprint {
  val floor = grassFloor()
  val texture = TextureId.grass
  return Blueprint(
      even = listOf(
          slopeWrap to emptyBuilder,
          solidBlock() to solidCubeBuilder(forestWall(), dirtFloor()),
          solidDiagonal() to solidDiagonalBuilder(forestWall(), dirtDiagonalFloor())
      ),
      tiered = listOf(
          withSolidBase(squareRoom) to floorMesh(floor),
          withSolidBase(fullSlope) to newSlopedFloorMesh(Depiction(mesh = MeshId.fullSlope, texture = texture)),
          withDiagonalSolidBase(diagonalCornerBlock) to diagonalHalfFloorMesh(grassDiagonalFloor()),
          withSolidBase(cornerSlope) to mergeBuilders(cornerSlope(texture), treeBuilder(treeBranching()))
      )
          .map(applyBlockBuilderLevels)
  )
}
