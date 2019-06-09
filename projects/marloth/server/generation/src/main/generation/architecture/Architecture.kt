package generation.architecture

import mythic.ent.IdSource
import mythic.spatial.Quaternion
import physics.Body
import scenery.MeshId
import scenery.Textures
import simulation.*

fun placeArchitecture(realm: Realm) = { nextId: IdSource ->
  realm.nodeList.map { node ->
    val id = nextId()
    Hand(
        id = id,
        depiction = Depiction(
            type = DepictionType.staticMesh,
            mesh = MeshId.circleFloor,
            texture = Textures.checkers
        ),
        body = Body(
            id = id,
            position = node.position,
            orientation = Quaternion(),
            node = node.id
        )
    )
  }
}
