package generation.architecture.biomes

import generation.architecture.blocks.*
import generation.architecture.building.*
import generation.architecture.connecting.Sides
import generation.architecture.engine.Builder
import generation.architecture.matrical.*
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.cellHalfLength

fun grassFloor(): Depiction =
    Depiction(
        mesh = MeshId.squareFloor,
        texture = TextureId.grass
//        mesh = MeshId.grassFloor
    )

fun grassDiagonalFloor(): Depiction =
    Depiction(
        mesh = MeshId.squareFloorHalfDiagonal,
        texture = TextureId.grass
//        mesh = MeshId.grassDiagonalFloor
    )

fun dirtFloor(): Depiction =
    Depiction(
        mesh = MeshId.squareFloor,
        texture = TextureId.bricks
//        mesh = MeshId.dirtFloor
    )

fun dirtDiagonalFloor(): Depiction =
    Depiction(
        mesh = MeshId.squareFloorHalfDiagonal,
        texture = TextureId.bricks
//        mesh = MeshId.dirtDiagonalFloor
    )

fun forestWall(): Depiction =
    Depiction(
        mesh = MeshId.squareWall,
        texture = TextureId.bricks
//        mesh = MeshId.dirtWall
    )

fun treeBranching(): Depiction =
    Depiction(
//        mesh = MeshId.squareFloor,
//        texture = TextureId.grass
        mesh = MeshId.branchingTree
    )

fun generalForestBuilder(): Builder =
    floorMesh(grassFloor())

fun withSolidBase(block: Block) =
    block.copy(
        sides = block.sides
            .plus(
//                Direction.down to Sides.solid
                    Direction.down to Sides.solidRequired
            )
    )

fun withSolidBase(tieredBlock: TieredBlock): TieredBlock = { level ->
  withSolidBase(tieredBlock(level)!!)
}

fun withDiagonalSolidBase(block: Block) =
    block.copy(
        sides = block.sides
            .plus(
                Direction.down to Sides.solidDiagonalVerticalRequired
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
          withSolidBase(squareRoom) to floorMesh(floor) + prop(Depiction(mesh = MeshId.lampPost), Vector3(0f, 0f, -cellHalfLength)),
          withSolidBase(fullSlope) to newSlopedFloorMesh(Depiction(mesh = MeshId.fullSlope, texture = texture)),
          withDiagonalSolidBase(diagonalCornerBlock) to diagonalHalfFloorMesh(grassDiagonalFloor()),
          withSolidBase(cornerSlope) to cornerSlope(texture) + prop(treeBranching(), Vector3(0f, 0f, -3.5f))
      )
          .map(applyBlockBuilderLevels)
  )
}
