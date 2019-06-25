package generation.architecture

import generation.abstracted.Graph
import generation.abstracted.nodeNeighbors
import generation.getNodeDistance
import generation.structure.wallHeight
import mythic.ent.Id
import mythic.spatial.*
import physics.Body
import physics.getLookAtAngle
import physics.voidNodeId
import scenery.MeshId
import scenery.Shape
import scenery.TextureId
import simulation.*
import kotlin.math.ceil

private val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

typealias MeshInfoMap = Map<MeshId, Shape>

fun newArchitectureMesh(meshInfo: MeshInfoMap, mesh: MeshId, position: Vector3, scale: Vector3 = Vector3.unit,
                        orientation: Quaternion = Quaternion(),
                        texture: TextureId = TextureId.checkersBlackWhite): Hand {
  val shape = meshInfo[mesh]!!
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh,
          texture = texture
      ),
      body = Body(
          position = position,
          orientation = orientation,
          node = voidNodeId,
          scale = scale
      ),
      collisionShape = shape
  )
}

fun alignWithCeiling(meshInfo: MeshInfoMap) = { meshId: MeshId ->
  val height = meshInfo[meshId]!!.shapeHeight
  Vector3(0f, 0f, -height / 2f)
}

fun alignWithFloor(meshInfo: MeshInfoMap) = { meshId: MeshId ->
  val height = meshInfo[meshId]!!.shapeHeight
  Vector3(0f, 0f, height / 2f)
}

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

val placeRoomFloors: (MeshInfoMap, Graph) -> List<Hand> = { meshInfo, graph ->
  roomNodes(graph)
      .map { node ->
        val horizontalScale = (node.radius + 1f) * 2f
        val mesh = MeshId.circleFloor
        newArchitectureMesh(
            meshInfo = meshInfo,
            mesh = MeshId.circleFloor,
            position = node.position + floorOffset + alignWithCeiling(meshInfo)(mesh),
            scale = Vector3(horizontalScale, horizontalScale, 1f),
            orientation = Quaternion()
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

val placeTunnelFloors: (MeshInfoMap, Graph) -> List<Hand> = { meshInfo, graph ->
  tunnelNodes(graph)
      .flatMap { node ->
        val segmentLength = 2f
        val info = getTunnelInfo(graph, node.id, segmentLength)

        val orientation = Quaternion().rotateZ(getLookAtAngle(info.vector))
        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod)
          val mesh = MeshId.longStep
          newArchitectureMesh(
              meshInfo = meshInfo,
              mesh = mesh,
              position = info.start + info.vector * stepOffset + floorOffset + minor + alignWithCeiling(meshInfo)(mesh),
              scale = Vector3.unit,
              orientation = orientation
          )
        }

      }
}

val placeTunnelWalls: (MeshInfoMap, Graph) -> List<Hand> = { meshInfo, graph ->
  tunnelNodes(graph)
      .flatMap { node ->
        val segmentLength = 4f
        val info = getTunnelInfo(graph, node.id, segmentLength)
        val lookAtAngle = getLookAtAngle(info.vector)
        val halfWidth = 2f

        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val mesh = MeshId.squareWall
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod)
          listOf(-1f, 1f).map { sideMod ->
            val orientation = Quaternion().rotateZ(lookAtAngle + sideMod * Pi / 2f)
            val sideOffset = Vector3(info.vector.y, -info.vector.x, 0f) * (sideMod + minorMod) * halfWidth
            newArchitectureMesh(
                meshInfo = meshInfo,
                mesh = mesh,
                position = info.start + info.vector * stepOffset + floorOffset + sideOffset + alignWithFloor(meshInfo)(mesh),
                scale = Vector3.unit,
                orientation = orientation
            )
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

val placeRoomWalls: (MeshInfoMap, Graph) -> List<Hand> = { meshInfo, graph ->
  roomNodes(graph)
      .flatMap { node ->
        val doorwayAngles = getDoorwayAngles(graph, node)

        val stripCount = doorwayAngles.size
        doorwayAngles.mapIndexed { firstIndex, firstAngle ->
          val secondIndex = (firstIndex + 1) % stripCount
          val secondDoorway = doorwayAngles[secondIndex]
          val secondAngle = if (secondDoorway <= firstAngle)
            secondDoorway + Pi * 2f
          else
            secondDoorway

          val angleLength = secondAngle - firstAngle
          val mesh = MeshId.squareWall
          val segmentLength = 4f / node.radius
          val margin = 1.6f / node.radius

          createSeries(angleLength, segmentLength, margin) { step, stepOffset ->
            val wallAngle = firstAngle + stepOffset
            val wallPosition = node.position + projectVector3(wallAngle, node.radius, node.position.z)
            val orientation = Quaternion().rotateZ(wallAngle)
            newArchitectureMesh(
                meshInfo = meshInfo,
                mesh = mesh,
                position = wallPosition + floorOffset + alignWithFloor(meshInfo)(mesh),
                scale = Vector3.unit,
                orientation = orientation
            )
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

fun placeArchitecture(meshInfo: MeshInfoMap, graph: Graph): WorldTransform =
    addHands(architectureSteps.flatMap { it(meshInfo, graph) })