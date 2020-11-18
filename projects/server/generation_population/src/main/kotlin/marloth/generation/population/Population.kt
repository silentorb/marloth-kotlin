package marloth.generation.population

import generation.architecture.engine.GenerationConfig
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.timing.FloatCycle
import simulation.characters.newPlayerAndCharacter
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.WorldInput
import simulation.misc.getPlayerStart
import simulation.misc.lightHandsFromDepictions

fun populateWorld(nextId: IdSource, config: GenerationConfig, graph: Graph): List<IdHand> {
  val definitions = config.definitions
  val playerCount = getDebugString("INITIAL_PLAYER_COUNT")?.toInt() ?: 1
  val hands = (1..playerCount)
      .flatMap { newPlayerAndCharacter(nextId, definitions, graph) }
//      .plus(populateRooms(config, nextId, input.dice, grid))
      .plus(listOf(
          IdHand(
              id = nextId(),
              hand = Hand(
                  cycleFloat = FloatCycle(0.006f, 0f)
              )
          ),
          IdHand(
              id = nextId(),
              hand = Hand(
                  cycleFloat = FloatCycle(0.002f, 0.2f)
              )
          )
      ))

  return hands
      .plus(lightHandsFromDepictions(definitions.lightAttachments, hands))
}
