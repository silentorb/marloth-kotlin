package marloth.integration.scenery

import marloth.clienting.rendering.GameScene
import marloth.clienting.rendering.createCamera
import marloth.clienting.rendering.getMovementRangeLayer
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.scenery.Scene
import simulation.main.Deck
import simulation.misc.Definitions

fun createScene(definitions: Definitions, deck: Deck): (Id) -> GameScene = { player ->
  val camera = createCamera(deck, player)
  val thirdPersonRig = deck.thirdPersonRigs[player]
  val equipmentLayer = if (thirdPersonRig == null)
    getPlayerEquipmentLayer(definitions, deck, player, camera)
  else
    null

  val targetingLayer = getTargetingLayer(deck, player)
  val movementRangeLayer = getMovementRangeLayer(definitions, deck, player)

  val layers = listOf(
      SceneLayer(
          elements = gatherBackground(deck.cyclesFloat, camera.position),
          useDepth = false
      ),
      SceneLayer(
          elements = gatherVisualElements(definitions, deck, player, thirdPersonRig),
          useDepth = true
      ),
      SceneLayer(
          elements = gatherParticleElements(deck, camera.position),
          useDepth = false
      )
  ) + listOfNotNull(movementRangeLayer, equipmentLayer, targetingLayer)

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
