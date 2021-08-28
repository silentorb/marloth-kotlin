package marloth.integration.generation

import generation.general.distributeToRaritySlots
import generation.architecture.engine.GenerationConfig
import marloth.clienting.editing.biomeIds
import marloth.definition.misc.monsterDistributions
import marloth.definition.misc.monsterLimit
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
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
              origin = transform.translation(),
              attributes = setOf(SpiritAttributes.isAggressive)
          ),
          newKnowledge()
      )
}

fun populateNewMonsters(definitions: Definitions, locations: List<Matrix>, nextId: IdSource, dice: Dice): List<NewHand> {
//  println("Monster count: ${locations.size}")
  return if (locations.none())
    listOf()
  else {
    val distributions = distributeToRaritySlots(dice, locations.size, monsterDistributions())
    locations
        .zip(distributions) { transform, definitionName ->
          val definition = definitions.professions[definitionName]!!
          placeMonster(definitions, dice, definition, nextId, transform.translate(Vector3(0f, 0f, 0.5f)))
        }
  }
}

fun populateNewMonsters(config: GenerationConfig, nextId: IdSource, dice: Dice, slots: SlotMap): List<NewHand> {
  val monsterLocations = slots
      .map { it.value.transform }

  return populateNewMonsters(config.definitions, monsterLocations, nextId, dice)
}

fun selectSlots(dice: Dice, slots: SlotMap, limit: Int): SlotMap {
  val count = min(limit, slots.size)
  return dice.take(slots.entries, count).associate { it.key to it.value }
}

fun selectSlots(attributes: Collection<String>, limit: (Int) -> Int): SlotSelector = { config, slots ->
  val filteredSlots = slots.filter { it.value.attributes.containsAll(attributes) }
  selectSlots(config.dice, filteredSlots, limit(config.cellCount))
}

fun populateMonsters(config: DistributionConfig, slots: SlotMap): SlotMap {
  val maxMonsters = min(monsterLimit(), config.cellCount / max(2, 16 - config.level))
  val groundSlots = slots.filter { it.value.attributes.contains(SlotTypes.ground) }
  return selectSlots(config.dice, groundSlots, maxMonsters)
}

fun distributeItemHands(config: GenerationConfig, nextId: IdSource, dice: Dice, slots: SlotMap): List<NewHand> {
  val itemDefinitions = filterPropGraphs(config, setOf(DistAttributes.floor, DistAttributes.food))
  return slots.flatMap { (_, slot) ->
    val itemDefinition = dice.takeOne(itemDefinitions)
    graphToHands(config.resourceInfo, nextId, itemDefinition, slot.transform)
  }
}

fun distributeBasicProps(config: GenerationConfig, nextId: IdSource, dice: Dice, slots: SlotMap): List<NewHand> {
  return slots.flatMap { (_, slot) ->
    slotToHands(config, nextId, dice, slot) {
      it
          .minus(listOf(DistAttributes.floor, DistAttributes.wall))
          .minus(biomeIds)
          .none()
    }
  }
}

val groundSlots = setOf(SlotTypes.ground)

val distributors = listOf(
    Distributor(::distributeLightSlots, ::distributeLightHands),
    Distributor(::populateMonsters, ::populateNewMonsters),
    Distributor(selectSlots(groundSlots) { it / 10 }, ::distributeItemHands),
    Distributor(selectSlots(groundSlots) { it * 2 / 3 }, ::distributeBasicProps),
)

fun populateDistributions(nextId: IdSource, config: GenerationConfig, dice: Dice, slots: SlotMap, cellCount: Int): List<NewHand> {
  val level = getDebugInt("WORLD_LEVEL") ?: config.level
  val distributionConfig = DistributionConfig(
      cellCount = cellCount,
      level = level,
      dice = dice,
  )

  return distributors
      .fold(listOf<NewHand>() to slots) { (hands, slots), distributor ->
        val selectedSlots = distributor.select(distributionConfig, slots)
        val newHands = distributor.generate(config, nextId, dice, selectedSlots)
        Pair(hands + newHands, slots - selectedSlots.keys)
      }
      .first
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
