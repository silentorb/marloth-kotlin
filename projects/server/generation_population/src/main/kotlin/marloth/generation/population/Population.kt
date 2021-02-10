package marloth.generation.population

import generation.abstracted.distributeToSlots
import generation.architecture.engine.GenerationConfig
import marloth.definition.data.characterDefinitions
import marloth.definition.data.miscellaneousDefinitions
import marloth.definition.misc.enemyDistributions
import marloth.definition.misc.monsterLimit
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.ent.scenery.nodesToElements
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.timing.FloatCycle
import simulation.characters.CharacterDefinition
import simulation.characters.newCharacter
import simulation.characters.newPlayerAndCharacter
import simulation.intellect.Spirit
import simulation.intellect.SpiritAttributes
import simulation.intellect.assessment.newKnowledge
import simulation.main.NewHand
import simulation.misc.*
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

data class ExpansionContext(
    val definitions: Definitions,
    val graph: Graph,
    val nextId: IdSource,
)

typealias NodeExpansion = (ExpansionContext, Key) -> NewHand
typealias NodeExpansionMap = Map<Key, NodeExpansion>

fun characterDefinitionExpansions(): NodeExpansionMap =
    characterDefinitions()
        .mapValues { (_, definition) ->
          { context, node ->
            newCharacter(context.nextId, context.definitions, definition, context.graph, node)
                .plusComponents(
                    Spirit(),
                    newKnowledge()
                )
          }
        }

fun miscellaneousExpansions(): NodeExpansionMap =
    miscellaneousDefinitions().mapValues { (_, hand) ->
      { _, node ->
        hand.plusComponents(
            NodeReference(node)
        )
      }
    }

fun graphToHands(definitions: Definitions, nextId: IdSource, graph: Graph): List<NewHand> {
  val expansions =
      characterDefinitionExpansions() +
          miscellaneousExpansions()

  val typeEntries = graph.filter { it.property == SceneProperties.type && expansions.containsKey(it.target) }
  val context = ExpansionContext(definitions, graph, nextId)
  return typeEntries.map { entry ->
    val expansion = expansions[entry.target]!!
    expansion(context, entry.source)
  }
}

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
        .zip(distributions) { location, definitionName ->
          val definition = definitions.professions[definitionName]!!
          placeMonster(definitions, definition, nextId, location)
        }
  }
}

fun populateMonsters(nextId: IdSource, definitions: Definitions, dice: Dice, graph: Graph, cellCount: Int): List<NewHand> {
  val spawners = nodeAttributes(graph, Entities.monsterSpawn)
  val count = min(monsterLimit(), min(spawners.size, cellCount / 15))
  val locations = dice.take(spawners, count)
      .map { getNodeTransform(graph, it) }

  return populateMonsters(definitions, locations, nextId, dice)
}

fun populateWorld(nextId: IdSource, config: GenerationConfig, dice: Dice, graph: Graph): List<NewHand> {
  val definitions = config.definitions
  val playerCount = getDebugInt("INITIAL_PLAYER_COUNT") ?: 1
  val elementGroups = nodesToElements(config.meshShapes, graph)
  val lights = elementGroups.flatMap { it.lights }
  val hands = (1..playerCount)
      .flatMap { newPlayerAndCharacter(nextId, definitions, graph) }
      .plus(graphToHands(definitions, nextId, graph))
      .plus(cycleHands(nextId))
      .plus(populateMonsters(nextId, definitions, dice, graph, config.cellCount))
      .plus(lights.map {
        NewHand(listOf(
            it.copy(isDynamic = false)
        ))
      })

  return hands
}
