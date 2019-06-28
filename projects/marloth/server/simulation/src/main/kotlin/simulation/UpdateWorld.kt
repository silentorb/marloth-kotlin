package simulation

import intellect.Spirit
import intellect.execution.pursueGoals
import intellect.updateAiState
import mythic.ent.*
import physics.BulletState
import physics.Collisions
import physics.updateBodies
import physics.updateBulletPhysics
import randomly.Dice
import simulation.input.updatePlayer
import simulation.particles.updateParticleEffect

const val simulationDelta = 1f / 60f

fun getFinished(deck: Deck): List<Id> {
  return deck.missiles.values
      .filter { isFinished(it) }
      .map { it.id }
//      .plus(deck.characters.values
//          .filter { !it.isAlive && !isPlayer(deck, it) }
//          .map { it.id })
}

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions
)

fun aliveSpirits(deck: Deck): Table<Spirit> =
    deck.spirits.filterKeys {
      val character = deck.characters[it]!!
      character.isAlive
    }

fun generateIntermediateRecords(world: World, commands: Commands): Intermediate =
    Intermediate(
        commands = commands,
        activatedAbilities = getActivatedAbilities(world, commands),
        collisions = listOf()
    )

fun updateEntities(dice: Dice, animationDurations: AnimationDurationMap, world: World, data: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, activatedAbilities, collisionMap) = data

      val bodies = updateBodies(world.copy(deck = deck), commands, collisionMap)
      val bodyWorld = world.copy(
          deck = deck.copy(bodies = bodies)
      )

      deck.copy(
          ambientSounds = updateAmbientAudio(dice, deck),
          bodies = bodies,
          depictions = mapTable(deck.depictions, updateDepiction(bodyWorld, animationDurations)),
          characters = mapTableValues(deck.characters, updateCharacter(bodyWorld, collisionMap, commands, activatedAbilities)),
          missiles = mapTableValues(deck.missiles, updateMissile(bodyWorld, collisionMap, simulationDelta)),
          particleEffects = mapTableValues(deck.particleEffects, bodyWorld.deck.bodies, updateParticleEffect(dice, simulationDelta)),
          players = mapTableValues(deck.players, updatePlayer(data.commands)),
          spirits = mapTableValues(deck.spirits, updateAiState(bodyWorld, simulationDelta))
      )
    }

val removeEntities: (Deck) -> Deck = { deck ->
  val finished = getFinished(deck)
  removeEntities(deck, finished)
}

fun newEntities(world: World, nextId: IdSource, data: Intermediate): (Deck) -> Deck = { deck ->
  deck.plus(getNewMissiles(world.copy(deck = deck), nextId, data.activatedAbilities))
}

fun updateWorldDeck(animationDurations: AnimationDurationMap, commands: Commands, delta: Float): (World) -> World =
    { world ->
      val nextId: IdSource = newIdSource(world.nextId)
      val data = generateIntermediateRecords(world, commands)

      val newDeck = pipe(world.deck, listOf(
          updateEntities(world.dice, animationDurations, world, data),
          removeEntities,
          newEntities(world, nextId, data)
      ))
      world.copy(
          deck = newDeck,
          nextId = nextId()
      )
    }

val updateGlobalDetails: (World) -> World = { world ->
  if (world.gameOver == null && isVictory(world))
    world.copy(
        gameOver = GameOver(
            winningFaction = misfitsFaction
        )
    )
  else
    world
}

fun updateWorld(bulletState: BulletState, animationDurations: AnimationDurationMap, world: World, playerCommands: Commands, delta: Float): World {
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck).values)
  val commands = playerCommands.plus(spiritCommands)
  return pipe(world, listOf(
      updateWorldDeck(animationDurations, commands, delta),
      updateGlobalDetails,
      updateBulletPhysics(bulletState)
  ))
}
