package marloth.generation.population

import generation.architecture.engine.GenerationConfig
import marloth.definition.data.characterDefinitions
import marloth.scenery.enums.CreatureId
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.scenery.filterByAttribute
import silentorb.mythic.ent.scenery.gatherChildren
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.timing.FloatCycle
import simulation.characters.newCharacter
import simulation.characters.newPlayerAndCharacter
import simulation.intellect.Spirit
import simulation.intellect.assessment.newKnowledge
import simulation.main.NewHand
import simulation.misc.Definitions
import simulation.misc.Entities
import simulation.misc.Factions
import kotlin.math.min

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
        .plusComponents(
            Spirit(),
            newKnowledge()
        )
  }
}

fun populateZone(nextId: IdSource, definitions: Definitions, dice: Dice, graph: Graph, zone: String): List<NewHand> {
  val childKeys = gatherChildren(graph, zone)
  val childEntries = graph.filter { childKeys.contains(it.source) }
  val spawners = filterByAttribute(childEntries, Entities.monsterSpawn)
  val count = min(spawners.size, spawners.size / 2 + 1)
  val selection = dice.take(spawners, count)
  return selection.map { spawner ->
    val definition = definitions.professions[CreatureId.hound]!!
    newCharacter(nextId, definitions, definition, graph, spawner, Factions.monsters)
        .plusComponents(
            Spirit(),
            newKnowledge()
        )
  }
}

fun populateWorld(nextId: IdSource, config: GenerationConfig, dice: Dice, graph: Graph): List<NewHand> {
  val definitions = config.definitions
  val playerCount = getDebugInt("INITIAL_PLAYER_COUNT") ?: 1
  val hands = (1..playerCount)
      .flatMap { newPlayerAndCharacter(nextId, definitions, graph) }
      .plus(graphToHands(definitions, nextId, graph))
      .plus(cycleHands(nextId))
      .plus(populateZone(nextId, definitions, dice, graph, "farm"))
  return hands
}
