package marloth.integration.scenery

import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.minMax
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.happenings.getActiveAction

fun getPlayerItemLayer(definitions: Definitions, deck: Deck, player: Id, camera: Camera): SceneLayer? {
  val equipped = getActiveAction(deck, player)
  return if (equipped == null)
    null
  else {
    val accessory = deck.accessories[equipped]!!
    val accessoryDefinition = definitions.accessories[accessory.type]!!
    val mesh = accessoryDefinition.mesh
    if (mesh == null)
      null
    else {
      val body = deck.bodies[player]!!
      val characterRig = deck.characterRigs[player]!!
      val relativeVelocity = characterRig.facingQuaternion.transform(body.velocity)
      // + relativeVelocity.x * 0.1f
      val motionOffsetX = minMax(characterRig.lookVelocity.x * 0.5f, -1f, 1f) +
          minMax(relativeVelocity.y * 0.2f, -1f, 1f)
//      val motionOffsetX = minMax(relativeVelocity.y * 0.1f , -1f, 1f)
      val motionOffsetY = minMax(characterRig.lookVelocity.y * 0.4f, -1f, 1f)
      val transform = Matrix.identity
          .translate(camera.position + camera.orientation.transform(Vector3(0.8f, -0.25f, -0.5f)))
          .rotate(camera.orientation)
          .rotateZ(Pi / 2f - motionOffsetX * 0.03f)
          .rotateX(motionOffsetY * 0.05f)
          .rotateY(motionOffsetX * 0.03f)

      SceneLayer(
          elements = listOf(
              ElementGroup(
                  meshes = listOf(
                      MeshElement(
                          id = 1L,
                          mesh = mesh,
                          transform = transform
                      )
                  )
              )
          ),
          useDepth = true,
          resetDepth = true
      )
    }
  }
}
