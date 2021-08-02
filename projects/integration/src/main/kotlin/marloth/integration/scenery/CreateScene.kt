package marloth.integration.scenery

import marloth.clienting.gui.hud.mobilityMovementRangeLayer
import marloth.clienting.rendering.*
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.GraphWrapper
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ResourceInfo
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.nodesToElements
import silentorb.mythic.ent.singleValueCache
import silentorb.mythic.lookinglass.*
import silentorb.mythic.scenery.Light
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck
import simulation.main.World

val graphElementCache = singleValueCache<Pair<ResourceInfo, GraphWrapper>, ElementGroups> { (resourceInfo, wrapper) ->
  val roots = getGraphRoots(wrapper.value)
  if (roots.any())
    nodesToElements(resourceInfo, wrapper.value)
  else
    listOf()
}

fun gatherLightsFromLayers(layers: List<SceneLayer>): List<Light> =
    layers
        .flatMap { layer ->
          layer.elements.flatMap { it.lights } +
              gatherLightsFromLayers(layer.children)
        }

fun createScene(resourceInfo: ResourceInfo, world: World): (Id) -> Scene = { player ->
  val deck = world.deck
  val definitions = world.definitions
  val graph = world.staticGraph
  val flyThrough = getDebugBoolean("FLY_THROUGH_CAMERA")
  val rigPlayer = deck.players[player]?.rig ?: 0L
  if (!deck.characters.containsKey(player))
    Scene(
        camera = emptyCamera(),
        lights = listOf(),
        lightingConfig = defaultLightingConfig(),
        layers = listOf(),
        filters = listOf()
    )
  else {
    val camera = createPlayerCamera(deck, rigPlayer)

    val characterRig = deck.characterRigs[rigPlayer]!!
    val equipmentLayer = if (characterRig.viewMode == ViewMode.firstPerson && !flyThrough)
      getPlayerEquipmentLayer(definitions, deck, player, camera)
    else
      null

    val character = deck.characters[player]
    val interactable = character?.canInteractWith
    val selected = if (interactable != null && character.isAlive) {
      val depiction = deck.depictions[interactable]
      if (depiction != null)
        convertSimpleDepiction(deck, interactable, depiction)
      else
        null
    } else
      null

    val selectedLayer = if (selected != null)
      SceneLayer(
          elements = listOf(ElementGroup(listOf(selected))),
          highlightColor = Vector4(1f, 0.9f, 0.3f, 1f)
      )
    else
      null

    val targetingLayer = getTargetingLayer(deck, player)
//    val movementRangeLayer = entanglingMovementRangeLayer(definitions, deck, player)
//        ?: if (getDebugBoolean("ENABLE_MOBILITY"))
//          mobilityMovementRangeLayer(definitions, deck, player)
//        else
//          null
    val movementRangeLayer = if (getDebugBoolean("ENABLE_MOBILITY"))
      mobilityMovementRangeLayer(definitions, deck, player)
    else
      null

    val mainElements = gatherVisualElements(definitions, deck, rigPlayer, characterRig) +
        graphElementCache(resourceInfo to graph)
//       + gridElementCache(world.realm.deck)

    val (transparentGroups, solidGroups) = mainElements
        .partition(::groupContainsTransparency)

    val layers = listOf(
        SceneLayer(
            depth = DepthMode.global,
            shadingMode = ShadingMode.deferred,
            children = listOfNotNull(
                SceneLayer(
                    elements = depthSort(camera, cullElementGroups(resourceInfo.meshShapes, camera, solidGroups)),
                ),
                SceneLayer(
                    elements = gatherBackground(deck.cyclesFloat, camera.position),
                    depth = DepthMode.globalNoWrite,
                    shadingMode = ShadingMode.none,
                ),
                SceneLayer(
                    elements = transparentGroups.sortedByDescending {
                      val location = it.meshes.firstOrNull()?.transform?.translation() ?: it.billboards.first().position
                      location.distance(camera.position)
                    },
                    depth = DepthMode.global,
                    shadingMode = ShadingMode.deferred,
                    blending = LayerBlending.premultiplied,
                ),
//                equipmentLayer,
            )
        ),
        SceneLayer(
            elements = gatherParticleElements(deck, camera.position),
            depth = DepthMode.none,
            shadingMode = ShadingMode.forward,
            blending = LayerBlending.premultiplied,
        ),
    ) + listOfNotNull(selectedLayer, movementRangeLayer, equipmentLayer)
    // + listOfNotNull(movementRangeLayer, equipmentLayer, targetingLayer)

    val elementLights = gatherLightsFromLayers(layers)

    val lights = if (getDebugBoolean("RENDER_NO_LIGHTS"))
      listOf()
    else
      mapLights(deck, player).plus(elementLights)

    Scene(
        camera = camera,
        lights = lights,
        lightingConfig = defaultLightingConfig(),
        layers = layers,
        filters = getScreenFilters(deck, player)
    )
  }
}
