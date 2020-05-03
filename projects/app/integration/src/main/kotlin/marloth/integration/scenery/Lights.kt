package marloth.integration.scenery

import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.ent.Id
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType
import silentorb.mythic.scenery.LightingConfig
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck

fun mapLights(deck: Deck, player: Id): List<Light> =
    deck.lights
        .map { (id, light) ->
          val transform = depictionTransform(deck.bodies, deck.characterRigs, id)
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
                color = Vector4(1f, 1f, 1f, 0.2f),
                offset = deck.bodies[player]!!.position + Vector3(0f, 0f, 2f),
                direction = Vector3(0f, 0f, 0f),
                range = 10f
            )
        ))

fun defaultLightingConfig() =
    LightingConfig(
        ambient = getDebugFloat("AMBIENT_LIGHT_LEVEL") ?: 0f
    )
