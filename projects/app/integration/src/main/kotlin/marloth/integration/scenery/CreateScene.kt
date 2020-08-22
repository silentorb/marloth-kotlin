package marloth.integration.scenery

import marloth.clienting.hud.entanglingMovementRangeLayer
import marloth.clienting.hud.mobilityMovementRangeLayer
import marloth.clienting.rendering.*
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.scenery.Scene
import simulation.main.Deck
import simulation.misc.Definitions

fun createScene(meshes: ModelMeshMap, models: Map<String, ModelFunction>, definitions: Definitions, deck: Deck): (Id) -> GameScene = { player ->
  val flyThrough = getDebugBoolean("FLY_THROUGH_CAMERA")
  if (!deck.characters.containsKey(player))
    GameScene(
        main = Scene(
            camera = emptyCamera(),
            lights = listOf(),
            lightingConfig = defaultLightingConfig()
        ),
        layers = listOf(),
        filters = listOf()
    )
  else {
    val camera = if (flyThrough)
      newFlyThroughCamera()
    else
      createPlayerCamera(deck, player)

    val characterRig = deck.characterRigs[player]!!
    val equipmentLayer = if (characterRig.viewMode == ViewMode.firstPerson && !flyThrough)
      getPlayerEquipmentLayer(definitions, deck, player, camera)
    else
      null

    val targetingLayer = getTargetingLayer(deck, player)
    val movementRangeLayer = entanglingMovementRangeLayer(definitions, deck, player)
        ?: if (getDebugBoolean("ENABLE_MOBILITY"))
          mobilityMovementRangeLayer(definitions, deck, player)
        else
          null

    val layers = listOf(
        SceneLayer(
            elements = gatherBackground(deck.cyclesFloat, camera.position),
            useDepth = false
        ),
        SceneLayer(
            elements = cullElementGroups(meshes, models, camera, gatherVisualElements(definitions, deck, player, characterRig)),
            useDepth = true
        ),
        SceneLayer(
            elements = listOf(),
            useDepth = true,
            attributes = setOf(marchingRenderLayer)
        ),
        SceneLayer(
            elements = gatherParticleElements(deck, camera.position),
            useDepth = false
        )
    ) + listOfNotNull(equipmentLayer)
    // + listOfNotNull(movementRangeLayer, equipmentLayer, targetingLayer)

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
}
