package simulation

import evention.DamageEvent
import evention.gatherEvents
import intellect.aliveSpirits
import intellect.execution.pursueGoals
import intellect.updateAiState
import mythic.ent.*
import physics.*
import randomly.Dice
import simulation.input.updatePlayer
import simulation.particles.updateParticleEffect

const val simulationDelta = 1f / 60f

fun getFinished(deck: Deck): List<Id> {
  return listOf()
//  return deck.missiles.values
//      .filter { isFinished(it) }
//      .map { it.id }
////      .plus(deck.characters.values
////          .filter { !it.isAlive && !isPlayer(deck, it) }
////          .map { it.id })
}

data class Events(
    val damage: List<DamageEvent>
)

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions,
    val events: Events
)

fun generateIntermediateRecords(bulletState: BulletState, deck: Deck, commands: Commands): Intermediate {
  val collisions = getBulletCollisions(bulletState, deck)
  return Intermediate(
      commands = commands,
      activatedAbilities = getActivatedAbilities(deck, commands),
      collisions = collisions,
      events = gatherEvents(deck, collisions)
  )
}

fun updateEntities(dice: Dice, animationDurations: AnimationDurationMap, world: World, data: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, activatedAbilities, collisionMap, events) = data

      val bodies = updateBodies(world.copy(deck = deck), commands, collisionMap)
      val bodyWorld = world.copy(
          deck = deck.copy(bodies = bodies)
      )

      deck.copy(
          ambientSounds = updateAmbientAudio(dice, deck),
          bodies = bodies,
          depictions = mapTable(deck.depictions, updateDepiction(bodyWorld, animationDurations)),
          characters = mapTableValues(deck.characters, updateCharacter(bodyWorld, commands, activatedAbilities, events.damage)),
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

fun updateWorldDeck(animationDurations: AnimationDurationMap, commands: Commands, bulletState: BulletState, delta: Float): (World) -> World =
    { world ->
      val (nextId, finalize) = newIdSource(world)
      val data = generateIntermediateRecords(bulletState, world.deck, commands)

      val newDeck = pipe(world.deck, listOf(
          updateEntities(world.dice, animationDurations, world, data),
          removeEntities,
          newEntities(world, nextId, data)
      ))
      finalize(world.copy(
          deck = newDeck
      ))
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
//  println(world.nextId)
  return pipe(world, listOf(
      updateBulletPhysics(bulletState),
      updateWorldDeck(animationDurations, commands, bulletState, delta),
      updateGlobalDetails
  ))
}
