package generation.architecture

import generation.structure.wallHeight
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import physics.Body
import physics.voidNodeId
import scenery.MeshId
import scenery.Shape
import scenery.Textures
import simulation.Depiction
import simulation.DepictionType
import simulation.Hand
import simulation.Realm

private val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

typealias MeshInfoMap = Map<MeshId, Shape>

fun newArchitectureMesh(meshInfo: MeshInfoMap, id: Id, mesh: MeshId, position: Vector3, scale: Vector3, orientation: Quaternion): Hand {
  val shape = meshInfo[mesh]!!
  return Hand(
      id = id,
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh,
          texture = Textures.checkers
      ),
      body = Body(
          id = id,
          position = position,
          orientation = orientation,
          node = voidNodeId,
          scale = scale
      ),
      collisionShape = shape
  )
}

fun placeArchitecture(meshInfo: MeshInfoMap, realm: Realm) = { nextId: IdSource ->
  val floorNodes = realm.nodeList
      .filter { node -> node.isWalkable }

  val (rooms, tunnels) = floorNodes
      .map { node ->
        val id = nextId()
        val horizontalScale = node.radius * 2f
        newArchitectureMesh(meshInfo, id, MeshId.circleFloor,
            position = node.position + floorOffset,
            scale = Vector3(horizontalScale, horizontalScale, 1f),
            orientation = Quaternion()
        )
//        val mesh = MeshId.circleFloor
//        val shape = meshInfo[mesh]!!
//        val horizontalScale = node.radius * 2f
//        Hand(
//            id = id,
//            depiction = Depiction(
//                type = DepictionType.staticMesh,
//                mesh = mesh,
//                texture = Textures.checkers
//            ),
//            body = Body(
//                id = id,
//                position = node.position + floorOffset,
//                orientation = Quaternion(),
//                node = node.id,
//                scale = Vector3(horizontalScale, horizontalScale, 1f)
//            ),
//            collisionShape = shape
//        )
      }
}
