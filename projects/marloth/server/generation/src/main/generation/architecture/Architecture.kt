package generation.architecture

import generation.abstracted.Graph
import generation.abstracted.nodeNeighbors
import generation.misc.biomeInfoMap
import generation.misc.getNodeDistance
import mythic.ent.Id
import mythic.spatial.*
import simulation.physics.old.getLookAtAngle
import randomly.Dice
import simulation.main.Hand
import simulation.main.WorldTransform
import simulation.main.addHands
import simulation.misc.MeshInfoMap
import simulation.misc.Node
import kotlin.math.ceil

fun <T> createSeries(gapSize: Float, segmentLength: Float, margin: Float, action: (Int, Float) -> T): List<T> {
  val length = gapSize - margin * 2f
  val stepCount = ceil(length / segmentLength).toInt()
  val overflow = stepCount * segmentLength - length
  val stepSize = if (stepCount > 1) segmentLength - overflow / (stepCount - 1) else 0f
  val start = margin + segmentLength / 2f
  return (0 until stepCount).map { step ->
    val stepOffset = start + stepSize * step.toFloat()
    action(step, stepOffset)
  }
}

fun tunnelNodes(graph: Graph) = graph.nodes.values
    .filter { node -> node.isWalkable && graph.tunnels.contains(node.id) }

fun roomNodes(graph: Graph) = graph.nodes.values
    .filter { node -> node.isWalkable && !graph.tunnels.contains(node.id) }

typealias Architect = (MeshInfoMap, Graph, Dice) -> List<Hand>

val placeRoomFloors: Architect = { meshInfo, graph, dice ->
  roomNodes(graph)
      .map { node ->
        val floorMeshAdjustment = 1f / 4f
        val horizontalScale = (node.radius + 1f) * 2f * floorMeshAdjustment
        val biome = biomeInfoMap[node.biome]!!
        val mesh = dice.getItem(biome.roomFloorMeshes)
        newArchitectureMesh(
            meshInfo = meshInfo,
            mesh = mesh,
            position = node.position + floorOffset + alignWithCeiling(meshInfo)(mesh),
            scale = Vector3(horizontalScale, horizontalScale, 1f),
            orientation = Quaternion(),
            texture = biome.floorTexture
        )
      }
}

data class TunnelInfo(
    val start: Vector3,
    val vector: Vector3,
    val length: Float
)

fun getTunnelInfo(graph: Graph, node: Id, segmentLength: Float): TunnelInfo {
  val neighbors = nodeNeighbors(graph.connections, node).map { graph.nodes[it]!! }
  val length = getNodeDistance(neighbors[0], neighbors[1])
  val vector = (neighbors[0].position - neighbors[1].position).normalize()
  val start = neighbors[1].position + vector * neighbors[1].radius

  return TunnelInfo(
      start = start,
      vector = vector,
      length = length
  )
}

val placeTunnelFloors: Architect = { meshInfo, graph, dice ->
  // Temporary improvement while the tunnel floor is rounded and the room floor isn't
  val tempHeightBump = 0.05f
  tunnelNodes(graph)
      .flatMap { node ->
        val segmentLength = 2f
        val info = getTunnelInfo(graph, node.id, segmentLength)
        val biome = biomeInfoMap[node.biome]!!
        val randomRotation = dice.getFloat(-0.1f, 1f)
        val orientation = Quaternion().rotateZ(getLookAtAngle(info.vector) + randomRotation)
        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod + tempHeightBump)
          val mesh = dice.getItem(biome.tunnelFloorMeshes)
          newArchitectureMesh(
              meshInfo = meshInfo,
              mesh = mesh,
              position = info.start + info.vector * stepOffset + floorOffset + minor + alignWithCeiling(meshInfo)(mesh),
              scale = Vector3.unit,
              orientation = orientation,
              texture = biome.floorTexture
          )
        }

      }
}

val placeTunnelWalls: Architect = { meshInfo, graph, dice ->
  tunnelNodes(graph)
      .flatMap { node ->
        val segmentLength = 4f
        val info = getTunnelInfo(graph, node.id, segmentLength)
        val lookAtAngle = getLookAtAngle(info.vector)
        val halfWidth = 2f
        val biome = biomeInfoMap[node.biome]!!
        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod)
          listOf(-1f, 1f)
              .filter { dice.getFloat() > biome.wallEnclosureRate }
              .map { sideMod ->
                val randomFlip = if (dice.getBoolean()) 1 else -1
                val sideOffset = Vector3(info.vector.y, -info.vector.x, 0f) * (sideMod + minorMod) * halfWidth
                val wallPosition = info.start + info.vector * stepOffset + sideOffset
                val wallAngle = lookAtAngle + sideMod * randomFlip * Pi / 2f
                newWall(meshInfo, dice, node, wallPosition, wallAngle)
              }
        }.flatten()
      }
}

fun getDoorwayAngles(graph: Graph, node: Node): List<Float> {
  val points = nodeNeighbors(graph.connections, node.id)
      .map {
        val neighbor = graph.nodes[it]!!
        neighbor.position
      }

  return points
      .map { atan(it.xy() - node.position.xy()) }
      .sorted()
}

fun getRoomSeriesAngleLength(firstIndex: Int, stripCount: Int, doorwayAngles: List<Float>, firstAngle: Float): Float {
  val secondIndex = (firstIndex + 1) % stripCount
  val secondDoorway = doorwayAngles[secondIndex]
  val secondAngle = if (secondDoorway <= firstAngle)
    secondDoorway + Pi * 2f
  else
    secondDoorway

  return secondAngle - firstAngle
}

val placeRoomWalls: Architect = { meshInfo, graph, dice ->
  roomNodes(graph)
      .flatMap { node ->
        val doorwayAngles = getDoorwayAngles(graph, node)
        val biome = biomeInfoMap[node.biome]!!

        val stripCount = doorwayAngles.size
        doorwayAngles.mapIndexed { firstIndex, firstAngle ->
          val angleLength = getRoomSeriesAngleLength(firstIndex, stripCount, doorwayAngles, firstAngle)
          val segmentLength = 4f / node.radius
          val margin = 1.6f / node.radius
          if (dice.getFloat() > biome.wallEnclosureRate) {
            listOf()
          } else {
            createSeries(angleLength, segmentLength, margin) { step, stepOffset ->
              //              val mesh = dice.getItem(biome.wallMeshes)
//              val wallData = wallDataMap[mesh]!!
//              val randomHorizontalFlip = getHorizontalFlip(dice, wallData)
              val wallAngle = firstAngle + stepOffset
              val wallPosition = node.position + projectVector3(wallAngle, node.radius, node.position.z)
//              val orientation = Quaternion().rotateZ(wallAngle + randomHorizontalFlip)
//              newArchitectureMesh(
//                  meshInfo = meshInfo,
//                  mesh = mesh,
//                  position = wallPosition + floorOffset + alignWithFloor(meshInfo)(mesh),
//                  scale = Vector3.unit,
//                  orientation = orientation
//              )
              newWall(meshInfo, dice, node, wallPosition, wallAngle)
            }
          }
        }.flatten()
      }
}

private val architectureSteps = listOf(
    placeRoomFloors,
    placeRoomWalls,
    placeTunnelFloors,
    placeTunnelWalls
)

fun placeArchitecture(meshInfo: MeshInfoMap, graph: Graph, dice: Dice): WorldTransform =
    addHands(architectureSteps.flatMap { it(meshInfo, graph, dice) })
