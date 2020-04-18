package marloth.generation.population

import generation.architecture.misc.MeshInfoMap
import marloth.definition.enums.MeshId
import silentorb.mythic.ent.IdSource
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.entities.Spinner
import simulation.happenings.Trigger
import simulation.main.Hand
import simulation.main.IdHand

fun newKey(meshes: MeshInfoMap, nextId: IdSource): (Vector3) -> IdHand = { location ->
  val id = nextId()
  val mesh = MeshId.key.name
  IdHand(
      id = id,
      hand = Hand(
          depiction = Depiction(
              type = DepictionType.staticMesh,
              mesh = mesh
          ),
          body = Body(
              position = location + Vector3(0f, 0f, 0.5f)
          ),
          collisionShape = CollisionObject(
              shape = meshes[mesh]!!.shape!!,
              isSolid = false
          ),
          trigger = Trigger(),
          spinner = Spinner(Pi),
          light = Light(
              type = LightType.point,
              color = Vector4(1f, 0.7f, 0f, 1f),
              offset = Vector3(0f, 0f, 1f),
              range = 6f
          )
      )
  )
}
