package marloth.integration.scenery

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.Meshes
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.*
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.spatial.*
import simulation.characters.EquipmentSlot
import simulation.happenings.getEquippedAction
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.misc.newVictoryKeyLight

fun getEquipmentTransform(camera: Camera, motionOffsetX: Float, motionOffsetY: Float, offsetX: Float, orientation: Quaternion): Matrix {
  return Matrix.identity
      .translate(camera.position + camera.orientation.transform(Vector3(0.8f, offsetX, -0.5f)))
      .rotate(orientation)
      .rotateX(motionOffsetY * 0.05f)
      .rotateY(motionOffsetX * 0.03f)
}

fun getEquippedMesh(definitions: Definitions, deck: Deck, player: Id): MeshName? {
  val equipped = getEquippedAction(definitions, deck, EquipmentSlot.attack, player)
  val accessory = deck.accessories[equipped]
  val accessoryDefinition = definitions.accessories[accessory?.type]
  return accessoryDefinition?.equippedMesh
}

fun getPlayerEquipmentLayer(definitions: Definitions, deck: Deck, player: Id, camera: Camera): SceneLayer? {
  val equippedMesh = getEquippedMesh(definitions, deck, player)
  val victoryKey = deck.accessories.entries
      .firstOrNull { (_, accessory) ->
        accessory.type == AccessoryIdOld.victoryKey && accessory.owner == player
      }
  return if (equippedMesh != null || victoryKey != null) {
    val body = deck.bodies[player]!!
    val characterRig = deck.characterRigs[player]!!
    val relativeVelocity = characterRig.facingOrientation.transform(body.velocity)
    val motionOffsetX = minMax(characterRig.firstPersonLookVelocity.x * 0.5f, -1f, 1f) +
        minMax(relativeVelocity.y * 0.2f, -1f, 1f)
    val motionOffsetY = minMax(characterRig.firstPersonLookVelocity.y * 0.4f, -1f, 1f)

    val elements = listOfNotNull(
        if (equippedMesh != null)
          ElementGroup(
              meshes = listOf(
                  MeshElement(
                      id = 1,
                      mesh = equippedMesh,
                      transform = getEquipmentTransform(camera, motionOffsetX, motionOffsetY, -0.25f,
                          Quaternion(camera.orientation)
                              .rotateZ(Pi / 2f - motionOffsetX * 0.03f)
                      )
                  )
              )
          )
        else
          null,
        if (victoryKey != null) {
          val transform = getEquipmentTransform(camera, motionOffsetX, motionOffsetY, 0.25f, deck.bodies[victoryKey.key]!!.orientation)
          val lightPosition = Vector3(1f, 0f, 0f).transform(transform)
          ElementGroup(
              meshes = listOf(
                  MeshElement(
                      id = 1,
                      mesh = Meshes.key,
                      transform = transform
                  )
              ),
              lights = listOf(
                  newVictoryKeyLight(lightPosition + Vector3(0f, 0f, 0.5f))
              )
          )
        } else
          null
    )

    SceneLayer(
        elements = elements,
        depth = DepthMode.local,
        shadingMode = ShadingMode.forward
    )
  } else
    null
}
