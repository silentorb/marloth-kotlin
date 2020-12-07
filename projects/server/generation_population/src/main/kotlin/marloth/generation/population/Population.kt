package marloth.generation.population

import generation.architecture.engine.GenerationConfig
import marloth.definition.data.Creatures
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.Text
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.timing.FloatCycle
import simulation.characters.newCharacter2
import simulation.characters.newPlayerAndCharacter
import simulation.entities.Interactable
import simulation.entities.WidgetCommand
import simulation.main.NewHand
import simulation.misc.Definitions
import simulation.misc.Factions
import simulation.misc.GameAttributes

fun cycleHands(nextId: IdSource) =
    listOf(
        NewHand(
            id = nextId(),
            components = listOf(
                FloatCycle(0.006f, 0f)
            )
        ),
        NewHand(
            id = nextId(),
            components = listOf(
                FloatCycle(0.002f, 0.2f)
            )
        )
    )

typealias EntityConstructor = (IdSource, Definitions, Graph, String) -> NewHand

val newFoodVendor: EntityConstructor = { nextId, definitions, graph, node ->
  val transform = getNodeTransform(graph, node)
  newCharacter2(nextId(), definitions, Creatures.foodVendor, Factions.neutral, transform.translation(), transform.rotation().z)
      .plusComponents(
          Interactable(
              primaryCommand = WidgetCommand(
                  text = Text.menu_talk,
                  clientCommand = ClientCommand.showMerchantView
              )
          )
      )
}

fun entityConstructors(): Map<String, EntityConstructor> = mapOf(
    GameAttributes.foodVendor to newFoodVendor
)

fun graphToHands(definitions: Definitions, nextId: IdSource, graph: Graph): List<NewHand> {
  val constructors = entityConstructors()
  val typeEntries = graph.filter { it.property == SceneProperties.type && constructors.containsKey(it.target) }
  return typeEntries.map { entry ->
    constructors[entry.target]!!(nextId, definitions, graph, entry.source)
  }
}

fun populateWorld(nextId: IdSource, config: GenerationConfig, graph: Graph): List<NewHand> {
  val definitions = config.definitions
  val playerCount = getDebugInt("INITIAL_PLAYER_COUNT") ?: 1
  val hands = (1..playerCount)
      .flatMap { newPlayerAndCharacter(nextId, definitions, graph) }
      .plus(graphToHands(definitions, nextId, graph))
      .plus(cycleHands(nextId))

  return hands
//      .plus(lightHandsFromDepictions(definitions.lightAttachments, hands))
}

//fun populateWorldGraph(nextId: IdSource, config: GenerationConfig): GraphStore {
//  val definitions = config.definitions
//  val playerCount = getDebugInt("INITIAL_PLAYER_COUNT") ?: 1
//  (1..playerCount)
//      .flatMap { newPlayerAndCharacter(nextId, definitions, graph) }
//}
