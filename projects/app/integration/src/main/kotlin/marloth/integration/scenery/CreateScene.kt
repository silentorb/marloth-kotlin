package marloth.integration.scenery

import marloth.clienting.rendering.GameScene
import silentorb.mythic.characters.ViewMode
import silentorb.mythic.characters.targeting.TargetTable
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.scenery.*
import simulation.main.Deck
import simulation.misc.Definitions

fun createScene(definitions: Definitions, deck: Deck, targetings: TargetTable): (Id) -> GameScene = { player ->
  val camera = createCamera(deck, player)
  val viewMode = deck.characterRigs[player]?.viewMode
  val equipmentLayer = if (viewMode == ViewMode.firstPerson)
      getPlayerEquipmentLayer(definitions, deck, player, camera)
  else
    null

  val targetingLayer = getTargetingLayer(deck,targetings, player)

  val layers = listOf(
      SceneLayer(
          elements = gatherBackground(deck.cyclesFloat, camera.position),
          useDepth = false
      ),
      SceneLayer(
          elements = gatherVisualElements(definitions, deck, player, deck.characterRigs[player]),
          useDepth = true
      ),
      SceneLayer(
          elements = gatherParticleElements(deck, camera.position),
          useDepth = false
      )
  ) + listOfNotNull(equipmentLayer, targetingLayer)

  val elementLights = layers.flatMap { layer ->
    layer.elements.flatMap { it.lights }
  }

  GameScene(
      main = Scene(
          camera = camera,
          lights = mapLights(deck, player).plus(elementLights),
          lightingConfig = defaultLightingConfig()
      ),
      layers = layers,
      filters = getScreenFilters(deck, player)
  )
}
