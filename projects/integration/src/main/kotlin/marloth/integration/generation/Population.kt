package marloth.integration.generation

import generation.abstracted.distributeToSlots
import generation.architecture.engine.GenerationConfig
import marloth.definition.misc.enemyDistributions
import marloth.definition.misc.monsterLimit
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.getGraphKeys
import silentorb.mythic.ent.scenery.expandInstances
import silentorb.mythic.ent.scenery.nodesToElements
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.timing.FloatCycle
import simulation.characters.CharacterDefinition
import simulation.characters.newCharacter
import simulation.characters.newPlayerAndCharacter
import simulation.intellect.Spirit
import simulation.intellect.SpiritAttributes
import simulation.intellect.assessment.newKnowledge
import simulation.main.Deck
import simulation.main.NewHand
import simulation.main.allHandsToDeck
import simulation.misc.*
import kotlin.math.max
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

fun placeMonster(definitions: Definitions, definition: CharacterDefinition, nextId: IdSource, transform: Matrix): NewHand {
  return newCharacter(nextId, definitions, definition, transform, Factions.monsters)
      .plusComponents(
          Spirit(
              post = transform.translation(),
              attributes = setOf(SpiritAttributes.isAggressive)
          ),
          newKnowledge()
      )
}

fun populateMonsters(definitions: Definitions, locations: List<Matrix>, nextId: IdSource, dice: Dice): List<NewHand> {
  println("Monster count: ${locations.size}")
  return if (locations.none())
    listOf()
  else {
    val distributions = distributeToSlots(dice, locations.size, enemyDistributions(), mapOf())
    locations
        .zip(distributions) { transform, definitionName ->
          val definition = definitions.professions[definitionName]!!
          placeMonster(definitions, definition, nextId, transform.translate(Vector3(0f, 0f, 0.5f)))
        }
  }
}

fun selectSlots(dice: Dice, slots: SlotMap, limit: Int): List<Map.Entry<Key, Slot>> {
  val count = min(limit, slots.size)
  return dice.take(slots.entries, count)
}

fun populateDistributions(nextId: IdSource, config: GenerationConfig, dice: Dice, slots: SlotMap, cellCount: Int): List<NewHand> {
  val definitions = config.definitions
  val graphLibrary = config.graphLibrary
  val level = getDebugInt("WORLD_LEVEL") ?: config.level
  val maxMonsters = min(monsterLimit(), cellCount / max(2, 16 - level))
  val monsterSlots = selectSlots(dice, slots, maxMonsters)
  val monsterLocations = monsterSlots
      .map { it.value.transform }

  val monsterHands = populateMonsters(definitions, monsterLocations, nextId, dice)
  val remainingSlots = slots - monsterSlots.map { it.key }

  val itemSlots = selectSlots(dice, remainingSlots, cellCount / 15)
  val itemDefinition = expandInstances(config.graphLibrary, graphLibrary["apple"]!!)
  val itemHands = itemSlots.flatMap { (_, slot) ->
    graphToHands(config.meshShapes, nextId, itemDefinition, slot.transform)
  }
  return monsterHands + itemHands
}

fun newPlayerCharacters(nextId: IdSource, definitions: Definitions, graph: Graph): List<NewHand> {
  val playerCount = getDebugInt("INITIAL_PLAYER_COUNT") ?: 1
  return (1..playerCount)
      .flatMap { newPlayerAndCharacter(nextId, definitions, graph) }
}

fun addNewPlayerCharacters(nextId: IdSource, config: GenerationConfig, graph: Graph, deck: Deck): Deck {
  val hands = newPlayerCharacters(nextId, config.definitions, graph)
  return allHandsToDeck(config.definitions, nextId, hands, deck)
}

fun populateWorld(nextId: IdSource, config: GenerationConfig, dice: Dice, graph: Graph): List<NewHand> {
  val elementGroups = nodesToElements(config.meshShapes, graph)
  val lights = elementGroups.flatMap { it.lights }
  val slots = gatherSlots(graph)
  val hands = cycleHands(nextId)
      .plus(populateDistributions(nextId, config, dice, slots, config.cellCount))
      .plus(lights.map {
        NewHand(listOf(
            it.copy(isDynamic = false)
        ))
      })

  return hands
}
