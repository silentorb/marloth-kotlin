package marloth.integration.scenery

import marloth.clienting.gui.hud.entanglingMovementRangeLayer
import marloth.clienting.gui.hud.mobilityMovementRangeLayer
import marloth.clienting.rendering.*
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.nodeToElements
import silentorb.mythic.ent.scenery.nodesToElements
import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.GameScene
import silentorb.mythic.lookinglass.Scene
import simulation.main.Deck
import simulation.misc.Definitions

fun createScene(meshes: ModelMeshMap, definitions: Definitions, deck: Deck, graph: Graph): (Id) -> GameScene = { player ->
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
    val camera = createPlayerCamera(deck, player)

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

    val roots = getGraphRoots(graph)
    val mainElements = gatherVisualElements(definitions, deck, player, characterRig) +
        if (roots.any())
          nodesToElements(mapOf(), setOf(), mapOf(), graph)
        else
          listOf()

    val layers = listOf(
        SceneLayer(
            elements = gatherBackground(deck.cyclesFloat, camera.position),
            useDepth = false
        ),
        SceneLayer(
            elements = cullElementGroups(meshes, camera, mainElements),
            useDepth = true
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
