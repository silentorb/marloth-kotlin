package marloth.integration.generation

import generation.abstracted.distributeToSlots
import generation.architecture.engine.GenerationConfig
import marloth.definition.misc.enemyDistributions
import marloth.definition.misc.monsterLimit
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.scenery.nodesToElements
import silentorb.mythic.physics.Body
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.timing.FloatCycle
import simulation.characters.*
import simulation.intellect.Spirit
import simulation.intellect.SpiritAttributes
import simulation.intellect.assessment.newKnowledge
import simulation.main.Deck
import simulation.main.NewHand
import simulation.main.allHandsToDeck
import simulation.misc.Definitions
import simulation.misc.DistAttributes
import simulation.misc.Factions
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

fun addHandBody(hand: NewHand, transform: Matrix): NewHand =
    hand.copy(
        components = hand.components
            .filter { !(it is Body) }
            .plus(
                Body(
                    position = transform.translation(),
                    orientation = Quaternion().rotateZ(transform.rotation().z)
                )
            )
    )

fun placeMonster(definitions: Definitions, dice: Dice, definition: CharacterDefinition, nextId: IdSource, transform: Matrix): NewHand {
  val definition2 = definition.copy(
      accessories = definition.accessories + equipCharacter(definitions, dice, definition)
  )
  return newCharacter(nextId, definitions, definition2, transform, Factions.monsters)
      .plusComponents(
          Spirit(
              post = transform.translation(),
              attributes = setOf(SpiritAttributes.isAggressive)
          ),
          newKnowledge()
      )
}

fun populateNewMonsters(definitions: Definitions, locations: List<Matrix>, nextId: IdSource, dice: Dice): List<NewHand> {
  println("Monster count: ${locations.size}")
  return if (locations.none())
    listOf()
  else {
    val distributions = distributeToSlots(dice, locations.size, enemyDistributions(), mapOf())
    locations
        .zip(distributions) { transform, definitionName ->
          val definition = definitions.professions[definitionName]!!
          placeMonster(definitions, dice, definition, nextId, transform.translate(Vector3(0f, 0f, 0.5f)))
        }
  }
}

fun populateOlderMonsters(monsterHands: Table<NewHand>, locations: List<Matrix>): List<NewHand> {
  return monsterHands.values
      .zip(locations) { hand, transform ->
        addHandBody(hand, transform)
      }
}

fun selectSlots(dice: Dice, slots: SlotMap, limit: Int): SlotMap {
  val count = min(limit, slots.size)
  return dice.take(slots.entries, count).associate { it.key to it.value }
}

fun populateMonsters(cellCount: Int, level: Int, dice: Dice, slots: SlotMap): SlotMap {
  val maxMonsters = min(monsterLimit(), cellCount / max(2, 16 - level))
  val groundSlots = slots.filter { it.value.attributes.contains(SlotTypes.ground) }
  return selectSlots(dice, groundSlots, maxMonsters)
}

fun distributeItemHands(nextId: IdSource, config: GenerationConfig, dice: Dice, itemSlots: SlotMap): List<NewHand> {
  val itemDefinitions = filterPropGraphs(config, setOf(DistAttributes.floor, DistAttributes.food))
  return itemSlots.flatMap { (_, slot) ->
    val itemDefinition = dice.takeOne(itemDefinitions)
    graphToHands(config.resourceInfo.meshShapes, nextId, itemDefinition, slot.transform)
  }
}

fun populateDistributions(nextId: IdSource, config: GenerationConfig, dice: Dice, slots: SlotMap, cellCount: Int): List<NewHand> {
  val definitions = config.definitions
  val level = getDebugInt("WORLD_LEVEL") ?: config.level

  val lightSlots = distributeLightSlots(slots)
  val lightHands = distributeLightHands(nextId, config, dice, lightSlots)
  val slots2 = slots - lightSlots.keys

  val monsterSlots = populateMonsters(cellCount, level, dice, slots2)
  val monsterLocations = monsterSlots
      .map { it.value.transform }

  val monsterHands = populateNewMonsters(definitions, monsterLocations, nextId, dice)
  val slots3 = slots2 - monsterSlots.map { it.key }

  val itemSlots = selectSlots(dice, slots3, cellCount / 10)
  val itemHands = distributeItemHands(nextId, config, dice, itemSlots)
  return monsterHands + itemHands + lightHands
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
  val elementGroups = nodesToElements(config.resourceInfo, graph)
  val lights = elementGroups.flatMap { it.lights }
  val slots = gatherSlots(graph, setOf(SlotTypes.ground, SlotTypes.wall))
  val hands = cycleHands(nextId)
      .plus(populateDistributions(nextId, config, dice, slots, config.cellCount))
      .plus(lights.map {
        NewHand(listOf(
            it.copy(isDynamic = false)
        ))
      })

  return hands
}
