package marloth.generation.population

import generation.architecture.engine.GenerationConfig
import marloth.definition.data.characterDefinitions
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.timing.FloatCycle
import simulation.characters.newCharacter
import simulation.characters.newPlayerAndCharacter
import simulation.main.NewHand
import simulation.misc.Definitions

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

fun graphToHands(definitions: Definitions, nextId: IdSource, graph: Graph): List<NewHand> {
  val characterDefinitions = characterDefinitions()
  val typeEntries = graph.filter { it.property == SceneProperties.type && characterDefinitions.containsKey(it.target) }
  return typeEntries.map { entry ->
    newCharacter(nextId, definitions, characterDefinitions[entry.target]!!, graph, entry.source)
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
