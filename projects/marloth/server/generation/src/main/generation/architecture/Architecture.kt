package generation.architecture

import generation.structure.wallHeight
import mythic.ent.IdSource
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import physics.Body
import scenery.MeshId
import scenery.Textures
import simulation.*

private val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

fun placeArchitecture(realm: Realm) = { nextId: IdSource ->
  realm.nodeList
      .filter { node -> node.isWalkable }
      .map { node ->
        val id = nextId()
        val horizontalScale = node.radius * 2f
        Hand(
            id = id,
            depiction = Depiction(
                type = DepictionType.staticMesh,
                mesh = MeshId.circleFloor,
                texture = Textures.checkers
            ),
            body = Body(
                id = id,
                position = node.position + floorOffset,
                orientation = Quaternion(),
                node = node.id,
                scale = Vector3(horizontalScale, horizontalScale, 1f)
            )
        )
      }
}
