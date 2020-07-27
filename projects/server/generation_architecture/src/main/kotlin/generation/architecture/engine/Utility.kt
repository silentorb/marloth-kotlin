package generation.architecture.engine

import generation.architecture.matrical.BlockBuilder
import generation.general.BiomeGrid
import generation.general.Block
import generation.general.Direction
import marloth.scenery.enums.MeshInfoMap
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Hand
import simulation.misc.*
import simulation.physics.CollisionGroups

fun applyBiomesToGrid(grid: MapGrid, biomeGrid: BiomeGrid): CellBiomeMap =
    grid.cells.mapValues { (cell, _) ->
      biomeGrid(absoluteCellPosition(cell))
    }

fun splitBlockBuilders(blockBuilders: Collection<BlockBuilder>): Pair<Set<Block>, Map<String, Builder>> =
    Pair(
        blockBuilders.map { it.block }.toSet(),
        blockBuilders.associate { Pair(it.block.name, it.builder ?: { listOf() }) }
    )

fun squareOffsets(length: Int): List<Vector3> {
  val step = cellLength / length
  val start = step / 2f

  return (0 until length).flatMap { y ->
    (0 until length).map { x ->
      Vector3(start + step * x, start + step * y, 0f)
    }
  }
}

fun newArchitectureMesh(meshes: MeshInfoMap, mesh: MeshName, position: Vector3,
                        orientation: Quaternion = Quaternion(),
                        node: Id = 0L,
                        texture: TextureName? = null,
                        scale: Vector3 = Vector3.unit): Hand {
  val meshInfo = meshes[mesh]!!
  val shape = meshInfo.shape
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh,
          texture = texture
      ),
      body = Body(
          position = position,
          orientation = orientation,
          nearestNode = node,
          scale = scale
      ),
      collisionShape = if (shape != null)
        CollisionObject(
            shape = shape,
            groups = CollisionGroups.static or CollisionGroups.affectsCamera or CollisionGroups.walkable,
            mask = CollisionGroups.staticMask
        )
      else
        null
  )
}

typealias VerticalAligner = (Float) -> (Float)

val alignWithCeiling: VerticalAligner = { height -> -height / 2f }
val alignWithFloor: VerticalAligner = { height -> height / 2f }

fun align(meshInfo: MeshInfoMap, aligner: VerticalAligner) = { mesh: MeshName ->
  val height = meshInfo[mesh]!!.shape!!.height
  Vector3(0f, 0f, aligner(height))
}

fun applyTurnsOld(turns: Int): Float =
    (turns.toFloat() - 1) * Pi * 0.5f

fun applyTurns(turns: Int): Float =
    turns.toFloat() * Pi * 0.5f

fun getTurnDirection(turns: Int): Direction =
    when ((turns + 4) % 4) {
      0 -> Direction.east
      1 -> Direction.north
      2 -> Direction.west
      3 -> Direction.south
      else -> throw Error("Shouldn't be here")
    }
