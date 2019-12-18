package marloth.scenery.creation

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.lookinglass.GameScene
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType
import silentorb.mythic.scenery.Scene
import marloth.scenery.enums.AccessoryId
import simulation.entities.hasEquipped
import simulation.main.Deck

fun mapLights(deck: Deck, player: Id) =
    deck.lights
        .map { (id, light) ->
          Light(
              type = LightType.point,
              color = light.color,
              position = deck.bodies[id]!!.position + Vector3(0f, 0f, 2.2f),
              direction = Vector3(0f, 0f, 0f),
              range = light.range
          )
        }
        .plus(listOfNotNull(
            if (hasEquipped(deck, player)(AccessoryId.candle.name))
              Light(
                  type = LightType.point,
                  color = Vector4(1f, 1f, 1f, 0.6f),
                  position = deck.bodies[player]!!.position + Vector3(0f, 0f, 2f),
                  direction = Vector3(0f, 0f, 0f),
                  range = 36f
              )
            else
              null
        ))

fun createScene(deck: Deck): (Id) -> GameScene = { player ->
  val camera = createCamera(deck, player)
  GameScene(
      main = Scene(
          camera = camera,
          lights = mapLights(deck, player)
      ),
      opaqueElementGroups = gatherVisualElements(deck, player, deck.players[player]!!),
      transparentElementGroups = gatherParticleElements(deck, camera.position),
      filters = if (!deck.characters[player]!!.isAlive)
        listOf<ScreenFilter>(
            { it.screenDesaturation.activate() },
            { it.screenColor.activate(Vector4(1f, 0f, 0f, 0.4f)) }
        )
      else
        listOf(),
      background = gatherBackground(deck.cyclesFloat, camera.position)
  )
}
