package marloth.clienting.hud

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.ModifierId
import silentorb.mythic.characters.defaultCharacterRadius
import silentorb.mythic.ent.Id
import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.Material
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck
import simulation.misc.Definitions

fun movementRangeLayer(color: Vector4, transform: Matrix) =
    SceneLayer(
        elements = listOf(
            ElementGroup(
                meshes = listOf(
                    MeshElement(
                        id = 1L,
                        mesh = MeshId.hollowCircle,
                        transform = transform,
                        material = Material(
                            color = color,
                            shading = false,
                            drawMethod = DrawMethod.lineLoop
                        )
                    )
                )
            )
        ),
        useDepth = false
    )

fun movementRangeLayer(deck: Deck, actor: Id, duration: Float, color: Vector4): SceneLayer {
  val speed = deck.characterRigs[actor]!!.maxSpeed
  val range = duration * speed + defaultCharacterRadius

  val center = deck.bodies[actor]!!.position
  val shape = deck.collisionObjects[actor]!!.shape
  val transform = Matrix.identity
      .translate(center - Vector3(0f, 0f, shape.height / 2f))
      .scale(range)

  return movementRangeLayer(color, transform)
}

fun mobilityMovementRangeLayer(definitions: Definitions, deck: Deck, actor: Id): SceneLayer? {
  val accessory = deck.accessories.entries
      .firstOrNull { it.value.owner == actor && it.value.type == AccessoryId.mobility }

  return if (accessory == null)
    null
  else {
    val action = deck.actions[accessory.key]!!
    val modifier = deck.modifiers.entries
        .firstOrNull { it.value.source == accessory.key }

    val duration = if (modifier != null)
      deck.timersFloat[modifier.key]!!.duration
    else if (action.cooldown == 0f)
      definitions.actions[accessory.value.type]!!.duration
    else
      0f

    if (duration == 0f)
      null
    else {
      val color = if (modifier == null)
        Vector4(0f, 1f, 1f, 0.1f)
      else
        Vector4(1f, 0.2f, 0.2f, 0.1f)

      movementRangeLayer(deck, actor, duration, color)
    }
  }
}

fun entanglingMovementRangeLayer(deck: Deck, actor: Id): SceneLayer? {
  val accessory = deck.accessories.entries
      .firstOrNull { it.value.owner == actor && it.value.type == ModifierId.entangling }

  return if (accessory == null)
    null
  else {
    val duration = deck.timersFloat[accessory.key]!!.duration
    movementRangeLayer(deck, actor, duration, Vector4(0f, 1f, 1f, 0.1f))
  }
}
