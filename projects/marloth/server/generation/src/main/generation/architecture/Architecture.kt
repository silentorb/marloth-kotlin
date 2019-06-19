package generation.architecture

import generation.abstracted.Graph
import generation.abstracted.nodeNeighbors
import generation.getNodeDistance
import generation.structure.wallHeight
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import physics.Body
import physics.getLookAtAngle
import physics.voidNodeId
import scenery.MeshId
import scenery.Shape
import scenery.TextureId
import simulation.*

private val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

typealias MeshInfoMap = Map<MeshId, Shape>

fun newArchitectureMesh(meshInfo: MeshInfoMap, mesh: MeshId, position: Vector3, scale: Vector3 = Vector3.unit,
                        orientation: Quaternion = Quaternion(),
                        texture: TextureId = TextureId.checkers): Hand {
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

//data class ArchitecturePlacement(
//    val mesh: MeshId,
//    val texture: TextureId? = null,
//    val position: Vector3,
//    val orientation: Quaternion,
//    val scale: Vector3 = Vector3.unit
//)
val placeFloors: (MeshInfoMap, Graph) -> List<Hand> = { meshInfo, graph ->
//fun placeFloors(graph: Graph): List<Hand> {
  val floorNodes = graph.nodes.values
      .filter { node -> node.isWalkable }

  val (tunnels, rooms) = floorNodes
      .partition { graph.tunnels.contains(it.id) }

  val alignment = { meshId: MeshId ->
    val height = meshInfo[meshId]!!.shapeHeight
    Vector3(0f, 0f, -height / 2f)
  }

  rooms
      .map { node ->
        val horizontalScale = node.radius * 2f
        val mesh = MeshId.circleFloor
        newArchitectureMesh(
            meshInfo = meshInfo,
            mesh = MeshId.circleFloor,
            position = node.position + floorOffset + alignment(mesh),
            scale = Vector3(horizontalScale, horizontalScale, 1f),
            orientation = Quaternion()
        )
      }
      .plus(tunnels
          .flatMap { node ->
            val segmentLength = 2.5f
            val neighbors = nodeNeighbors(graph.connections, node.id).map { graph.nodes[it]!! }
            val gapSize = getNodeDistance(neighbors[0], neighbors[1]) + segmentLength
            val stepCount = (gapSize / segmentLength).toInt() + 1 // One extra to overlap the room
            val stepSize = gapSize / (stepCount + 1).toFloat()
            val vector = (neighbors[0].position - neighbors[1].position).normalize()
            val start = neighbors[1].position + vector * (neighbors[1].radius + stepSize - segmentLength / 2f)
            val minorOffset = 0.001f

            val orientation = Quaternion().rotateZ(getLookAtAngle(vector))
            (0 until stepCount).map { step ->
              val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
              val minor = Vector3(0f, 0f, minorMod)
              val mesh = MeshId.longStep
              newArchitectureMesh(
                  meshInfo = meshInfo,
                  mesh = mesh,
                  position = start + vector * stepSize * step.toFloat() + floorOffset + minor + alignment(mesh),
                  scale = Vector3.unit,
                  orientation = orientation
              )
            }

          })
}

//val placeFloors: (MeshInfoMap, Graph) -> List<Hand> = { meshInfo, graph ->
//  placeFloors(graph)
//      .map {
//        val height = meshInfo[it.mesh]!!.shapeHeight
//        val shapeOffset = Vector3(0f, 0f, -height / 2f)
//        newArchitectureMesh(meshInfo, it.mesh,
//            position = it.position + shapeOffset,
//            scale = it.scale,
//            orientation = it.orientation
//        )
//      }
//}

private val architectureSteps = listOf(
    placeFloors
)

fun placeArchitecture(meshInfo: MeshInfoMap, graph: Graph): WorldTransform =
    addHands(architectureSteps.flatMap { it(meshInfo, graph) })
