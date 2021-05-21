package marloth.clienting.gui.hud

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.MeshId
import silentorb.mythic.characters.rigs.defaultCharacterRadius
import silentorb.mythic.ent.Id
import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.lookinglass.*
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.characters.getMoveSpeed
import simulation.main.Deck
import simulation.misc.Definitions

fun movementRangeLayer(color: Vector4, transform: Matrix) =
    SceneLayer(
        elements = listOf(
            ElementGroup(
                meshes = listOf(
                    MeshElement(
                        id = 1,
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
        depth = DepthMode.global
    )

fun movementRangeLayer(definitions: Definitions, deck: Deck, actor: Id, duration: Float, color: Vector4): SceneLayer {
  val speed = getMoveSpeed(definitions, deck)(actor)
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
      .firstOrNull { it.value.owner == actor && it.value.type == AccessoryIdOld.mobility }

  return if (accessory == null)
    null
  else {
    val action = deck.actions[accessory.key]!!
    val modifier = deck.accessories.entries
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

      movementRangeLayer(definitions, deck, actor, duration, color)
    }
  }
}

fun entanglingMovementRangeLayer(definitions: Definitions, deck: Deck, actor: Id): SceneLayer? {
  val accessory = deck.accessories.entries
      .firstOrNull { it.value.owner == actor && it.value.type == AccessoryIdOld.entangling }

  return if (accessory == null)
    null
  else {
    val duration = deck.timersFloat[accessory.key]!!.duration
    movementRangeLayer(definitions, deck, actor, duration, Vector4(0f, 1f, 1f, 0.1f))
  }
}
