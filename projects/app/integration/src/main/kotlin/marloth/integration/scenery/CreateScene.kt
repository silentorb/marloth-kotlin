package marloth.integration.scenery

import marloth.clienting.rendering.GameScene
import marloth.scenery.enums.AccessoryId
import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.scenery.*
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import simulation.combat.PlayerOverlayType
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.misc.hasEquipped

fun mapLights(deck: Deck, player: Id) =
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
        .plus(listOfNotNull(
            if (hasEquipped(deck, player)(AccessoryId.candle.name))
              Light(
                  type = LightType.point,
                  color = Vector4(1f, 1f, 1f, 0.2f),
                  offset = deck.bodies[player]!!.position + Vector3(0f, 0f, 2f),
                  direction = Vector3(0f, 0f, 0f),
                  range = 10f
              )
            else
              null
        ))

fun defaultLightingConfig() =
    LightingConfig(
        ambient = getDebugFloat("AMBIENT_LIGHT_LEVEL") ?: 0f
    )

fun bloodOverlay(strength: Float): ScreenFilter =
    { shaders, scale -> shaders.screenColor.activate(scale, Vector4(1f, 0f, 0f, strength)) }

fun getPlayerOverlays(deck: Deck, player: Id) =
    if (!deck.characters[player]!!.isAlive)
      listOf<ScreenFilter>(
          { shaders, scale -> shaders.screenDesaturation.activate(scale) },
          bloodOverlay(0.4f)
      )
    else
      deck.playerOverlays
          .filterValues { it.player == player }
          .mapNotNull { overlay ->
            when (overlay.value.type) {
              PlayerOverlayType.bleeding -> bloodOverlay(0.2f)
              else -> null
            }
          }

fun createScene(definitions: Definitions, deck: Deck): (Id) -> GameScene = { player ->
  val camera = createCamera(deck, player)
  GameScene(
      main = Scene(
          camera = camera,
          lights = mapLights(deck, player),
          lightingConfig = defaultLightingConfig()
      ),
      layers = listOf(
          SceneLayer(
              elements = gatherBackground(deck.cyclesFloat, camera.position),
              useDepth = false
          ),
          SceneLayer(
              elements = gatherVisualElements(definitions, deck, player, deck.players[player]!!),
              useDepth = true
          ),
          SceneLayer(
              elements = gatherParticleElements(deck, camera.position),
              useDepth = false
          )
      ) + listOfNotNull(getPlayerItemLayer(definitions, deck, player, camera)),
      filters = getPlayerOverlays(deck, player)
  )
}
