package marloth.integration.scenery

import marloth.clienting.gui.hud.mobilityMovementRangeLayer
import marloth.clienting.rendering.*
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.scenery.getGraphRoots
import silentorb.mythic.ent.scenery.nodesToElements
import silentorb.mythic.ent.singleValueCache
import silentorb.mythic.lookinglass.*
import simulation.main.Deck
import simulation.main.World

val graphElementCache = singleValueCache<Graph, ElementGroups> { graph ->
  val roots = getGraphRoots(graph)
  if (roots.any())
    nodesToElements(mapOf(), graph)
  else
    listOf()
}

val gridElementCache = singleValueCache<Deck, ElementGroup> { deck ->
  ElementGroup(
      meshes = deck.depictions
          .filter { !isComplexDepiction(it.value) }
          .mapNotNull {
            convertSimpleDepiction(deck, it.key, it.value)
          }
  )
}

fun createScene(meshes: ModelMeshMap, world: World): (Id) -> GameScene = { player ->
  val deck = world.deck
  val definitions = world.definitions
  val graph = world.staticGraph
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
//    val movementRangeLayer = entanglingMovementRangeLayer(definitions, deck, player)
//        ?: if (getDebugBoolean("ENABLE_MOBILITY"))
//          mobilityMovementRangeLayer(definitions, deck, player)
//        else
//          null
    val movementRangeLayer = if (getDebugBoolean("ENABLE_MOBILITY"))
      mobilityMovementRangeLayer(definitions, deck, player)
    else
      null

    val mainElements = gatherVisualElements(definitions, deck, player, characterRig) +
        graphElementCache(graph) +
        gridElementCache(world.realm.deck)

    val (particleGroups, solidGroups) = mainElements
        .partition { group -> group.billboards.any() }

    val layers = listOf(
        SceneLayer(
            elements = gatherBackground(deck.cyclesFloat, camera.position),
            useDepth = false
        ),
        SceneLayer(
            elements = cullElementGroups(meshes, camera, solidGroups),
            useDepth = true
        ),
        SceneLayer(
            elements = particleGroups.sortedByDescending { it.billboards.first().position.distance(camera.position) },
            useDepth = true
        ),
        SceneLayer(
            elements = gatherParticleElements(deck, camera.position),
            useDepth = false
        ),
    ) + listOfNotNull(movementRangeLayer, equipmentLayer)
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
