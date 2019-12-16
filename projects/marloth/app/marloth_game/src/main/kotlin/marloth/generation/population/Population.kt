package marloth.generation.population

import generation.architecture.misc.GenerationConfig
import generation.architecture.misc.calculateWorldScale
import generation.general.BiomeAttribute
import silentorb.mythic.debugging.getDebugSetting
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.toIdHands
import simulation.entities.Cycle
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.Realm
import simulation.misc.WorldInput

fun populateWorld(nextId: IdSource, config: GenerationConfig, input: WorldInput, realm: Realm): (Deck) -> List<IdHand> = { deck ->
  val playerNodeOld = realm.nodeTable.values.firstOrNull {
    config.biomes[it.biome]!!.attributes.contains(BiomeAttribute.placeOnlyAtStart)
  }

  if (playerNodeOld == null)
    throw Error("Biome configuration is missing placeOnlyAtStart")

  val grid = realm.grid
  val playerCell = getPlayerCell(grid)
  val scale = calculateWorldScale(input.boundary.dimensions)
  val occupantToHand = occupantPopulator(config, nextId)
  val playerCount = getDebugSetting("INITIAL_PLAYER_COUNT")?.toInt() ?: 1
  (1..playerCount).flatMap { newPlayer(nextId, config.definitions, grid, playerCell) }
      .plus(populateRooms(occupantToHand, input.dice, realm, playerNodeOld.id))
      .plus(toIdHands(nextId, placeWallLamps(deck, config, realm, input.dice, scale)))
      .plus(listOf(
          IdHand(
              id = nextId(),
              hand = Hand(
                  cycle = Cycle(0.006f, 0f)
              )
          ),
          IdHand(
              id = nextId(),
              hand = Hand(
                  cycle = Cycle(0.002f, 0.2f)
              )
          )
      ))
}
