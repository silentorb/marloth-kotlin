package marloth.generation.population

import generation.architecture.misc.GenerationConfig
import generation.architecture.misc.calculateWorldScale
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.IdSource
import silentorb.mythic.timing.FloatCycle
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.Realm
import simulation.misc.WorldInput
import simulation.misc.lightHandsFromDepictions

fun populateWorld(nextId: IdSource, config: GenerationConfig, input: WorldInput,
                  realm: Realm): (Deck) -> List<IdHand> = { deck ->
  val grid = realm.grid
  val definitions = config.definitions
  val playerCell = getPlayerCell(grid)
  val playerCount = getDebugString("INITIAL_PLAYER_COUNT")?.toInt() ?: 1
  val hands = (1..playerCount)
      .flatMap { newPlayer(nextId, definitions, grid, playerCell) }
      .plus(populateRooms(config, nextId, input.dice, realm))
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
  hands
      .plus(lightHandsFromDepictions(definitions.lightAttachments, hands))
}
