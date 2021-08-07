package marloth.integration.scenery

import marloth.clienting.rendering.getBodyTransform
import silentorb.mythic.ent.Id
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck

fun mapLights(deck: Deck, player: Id): List<Light> =
    deck.lights
        .filterValues { it.isDynamic }
        .map { (id, light) ->
          val transform = getBodyTransform(deck.bodies, id)
          Light(
              type = LightType.point,
              color = light.color,
              offset = light.offset.transform(transform),
              direction = Vector3(0f, 0f, 0f),
              range = light.range
          )
        }
        .plus(listOf(
            Light(
                type = LightType.point,
                color = Vector4(1f, 1f, 1f, 0.4f),
                offset = deck.bodies[player]!!.position + Vector3(0f, 0f, 2f),
                direction = Vector3(0f, 0f, 0f),
                range = 8f
            )
        ))
