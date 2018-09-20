package simulation

import intellect.pursueGoals
import intellect.updateAiState
import mythic.spatial.Pi2
import physics.Collision
import physics.Collisions
import physics.updateBodies
import simulation.changing.findCollisionWalls
import simulation.input.updatePlayers

fun <T> updateField(defaultValue: T, newValue: T?): T =
    if (newValue != null)
      newValue
    else
      defaultValue

fun simplifyRotation(value: Float): Float =
    if (value > Pi2)
      value % (Pi2)
    else if (value < -Pi2)
      -(Math.abs(value) % Pi2)
    else
      value

fun getFinished(world: World): List<Id> {
  return world.table.missiles.values
      .filter { isFinished(world.realm, world.bodyTable, it) }
      .map { it.id }
      .plus(world.characters
          .filter { isFinished(world, it) && !isPlayer(world, it) }
          .map { it.id })
//      .plus(world.bodies.filter { it.node == voidNode }.map { it.id })
}

fun removeFinished(deck: Deck, finishedIds: List<Id>): Deck {
  val IsActive = { entity: EntityLike -> !finishedIds.contains(entity.id) }

  return deck.copy(
      characters = deck.characters.filter(IsActive),
      missiles = deck.missiles.filter(IsActive),
      bodies = deck.bodies.filter(IsActive),
      spirits = deck.spirits.filter(IsActive)
  )
}

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions
)

fun generateIntermediateRecords(world: World, playerCommands: Commands, delta: Float): Intermediate {
  val spiritCommands = pursueGoals(world.spirits)
  val commands = playerCommands.plus(spiritCommands)
  val collisions: Collisions = world.bodies.flatMap { body ->
    val offset = body.velocity * delta
    val walls = findCollisionWalls(body.position, offset, world.realm, body.node)
    walls.map { Collision(body.id, null, it.wall, it.hitPoint, it.gap) }
  }
      .plus(getBodyCollisions(world.bodyTable, world.characterTable, world.missiles))
  val activatedAbilities = getActivatedAbilities(world, commands)

  return Intermediate(
      commands = commands,
      activatedAbilities = activatedAbilities,
      collisions = collisions
  )
}

fun updateEntities(animationDurations: AnimationDurationMap, deck: Deck, world: World, data: Intermediate): Deck {
  val (commands, activatedAbilities, collisionMap) = data

  return deck.copy(
      depictions = updateDepictions(animationDurations, world),
      bodies = updateBodies(world, commands, collisionMap),
      characters = updateCharacters(world, collisionMap, commands, activatedAbilities),
      missiles = updateMissiles(world, collisionMap),
      players = updatePlayers(deck.players, data.commands),
      spirits = deck.spirits.map { updateAiState(world, it) }
  )
}

fun removeEntities(deck: Deck, world: World): Deck {
  val finished = getFinished(world)
  return removeFinished(deck, finished)
}

fun newEntities(world: World, nextId: IdSource, data: Intermediate): Deck {
  return getNewMissiles(world, nextId, data.activatedAbilities)
}

fun updateWorldMain(animationDurations: AnimationDurationMap, deck: Deck, world: World, playerCommands: Commands, delta: Float): World {
  val nextId: IdSource = newIdSource(world.nextId)
  val data = generateIntermediateRecords(world, playerCommands, delta)
  val updatedDeck = updateEntities(animationDurations, deck, world, data)
  val finalDeck = removeEntities(updatedDeck, world)
      .plus(newEntities(world, nextId, data))

  return world.copy(
      deck = finalDeck,
      nextId = nextId()
  )
}

const val simulationDelta = 1f / 60f

private var deltaAggregator = 0f

fun updateWorld(animationDurations: AnimationDurationMap, world: World, commands: Commands, delta: Float): World {
  deltaAggregator += delta
  if (deltaAggregator > simulationDelta) {
    deltaAggregator -= simulationDelta

    // The above subtraction, this condition, and the modulus could all be handled by a single modulus
    // but if deltaAggregator is more than twice the simulationHertz then the simulation is not keeping
    // up and I want that to be handled as a special case, not incidentally handled by a modulus.
    if (deltaAggregator > simulationDelta) {
      println("Skipped a frame.  deltaAggregator = " + deltaAggregator + " simulationDelta = " + simulationDelta)
      deltaAggregator = deltaAggregator % simulationDelta
    }
    return updateWorldMain(animationDurations, world.deck, world, commands, simulationDelta)
  } else {
    return world
  }
}