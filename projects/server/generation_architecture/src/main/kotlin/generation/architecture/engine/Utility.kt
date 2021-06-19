package generation.architecture.engine

import generation.architecture.matrical.BlockBuilder
import generation.general.*
import marloth.scenery.enums.MeshInfoMap
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.getNodeValue
import silentorb.mythic.ent.getGraphValues
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.main.Hand
import simulation.misc.*
import simulation.physics.CollisionGroups

//fun applyBiomesToGrid(grid: MapGrid, biomeGrid: BiomeGrid): CellBiomeMap =
//    grid.cells.mapValues { (cell, _) ->
//      biomeGrid(absoluteCellPosition(cell))
//    }

fun splitBlockBuilders(blockBuilders: Collection<BlockBuilder>): Pair<Set<Block>, Map<String, Builder>> =
    Pair(
        blockBuilders.map { it.first }.toSet(),
        blockBuilders.associate { Pair(it.first.name, it.second) }
    )

fun squareOffsets(length: Int): List<Vector3> {
  val step = cellLength / length
  val start = step / 2f

  return (0 until length).flatMap { y ->
    (0 until length).map { x ->
      Vector3(start + step * x - cellHalfLength, start + step * y - cellHalfLength, -cellHalfLength)
    }
  }
}

fun newArchitectureMesh(meshes: MeshInfoMap, depiction: Depiction, position: Vector3,
                        orientation: Quaternion = Quaternion(),
                        scale: Vector3 = Vector3.unit): Hand {
  val meshInfo = meshes[depiction.mesh]
  val shape = meshInfo?.shape
  return Hand(
      body = Body(
          position = position,
          orientation = orientation,
          scale = scale
      ),
      collisionShape = if (shape != null)
        CollisionObject(
            shape = shape,
            groups = CollisionGroups.solidStatic,
            mask = CollisionGroups.staticMask
        )
      else
        null,
      depiction = depiction
  )
}

typealias VerticalAligner = (Float) -> (Float)

val alignWithCeiling: VerticalAligner = { height -> -height / 2f }
val alignWithFloor: VerticalAligner = { height -> height / 2f }

fun align(meshInfo: MeshInfoMap, aligner: VerticalAligner) = { mesh: MeshName? ->
  val height = meshInfo[mesh]?.shape?.height
  if (height != null)
    Vector3(0f, 0f, aligner(height))
  else
    Vector3.zero
}

fun applyTurnsOld(turns: Int): Float =
    (turns.toFloat() - 1) * Pi * 0.5f

fun applyTurns(turns: Int): Float =
    turns.toFloat() * Pi * 0.5f

tailrec fun expandSideGroups(sideGroups: Map<String, Set<String>>, value: Collection<String>, step: Int = 0): Collection<String> {
  if (step > 20)
    throw Error("Infinite loop detected with expanding side type groups")

  val groups = sideGroups.keys.intersect(value)
  return if (groups.none())
    value
  else {
    val next = (value - groups) + groups.flatMap { sideGroups[it]!! }
    expandSideGroups(sideGroups, next, step + 1)
  }
}

fun getCellDirection(graph: Graph, node: String): CellDirection? =
    getNodeValue<CellDirection>(graph, node, GameProperties.direction)

fun gatherSides(sideGroups: Map<String, Set<String>>, graph: Graph, sideNodes: List<String>): List<Pair<CellDirection, Side?>> =
    sideNodes
        .mapNotNull { node ->
          val mine = getNodeValue<String>(graph, node, GameProperties.mine)
          val initialOther = getGraphValues<String>(graph, node, GameProperties.other)
          val other = expandSideGroups(sideGroups, initialOther)
          val cellDirection = getCellDirection(graph, node)
          if (cellDirection == null)
            null
          else if (mine == null || other.none())
            cellDirection to null
          else {
            val height = getNodeValue<Int>(graph, node, GameProperties.sideHeight) ?: StandardHeights.first
            cellDirection to Side(
                mine = mine,
                other = other.toSet(),
                height = height,
            )
          }
        }
