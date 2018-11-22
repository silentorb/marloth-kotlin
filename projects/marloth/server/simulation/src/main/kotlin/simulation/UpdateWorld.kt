package simulation

import intellect.pursueGoals
import intellect.updateAiState
import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.newIdSource
import mythic.spatial.Pi2
import mythic.spatial.Vector3
import physics.Collision
import physics.Collisions
import physics.updateBodies
import physics.MovingBody
import physics.getWallCollisions
import physics.wallsInCollisionRange
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
      .filter { isFinished(it) }
      .map { it.id }
      .plus(world.characters
          .filter { isFinished(world, it) && !isPlayer(world, it) }
          .map { it.id })
}

fun removeFinished(deck: Deck, finishedIds: List<Id>): Deck {
  val isActive = { entity: Entity -> !finishedIds.contains(entity.id) }

  if (deck.missiles.any { !isActive(it) }) {
    val k = 0
  }

  return deck.copy(
      characters = deck.characters.filter(isActive),
      missiles = deck.missiles.filter(isActive),
      bodies = deck.bodies.filter(isActive),
      spirits = deck.spirits.filter(isActive)
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
  val collisions: Collisions = world.bodies
      .filter { it.velocity != Vector3.zero }
      .flatMap { body ->
        val offset = body.velocity * delta
        val wallsInRange = wallsInCollisionRange(world.realm, world.realm.nodeTable[body.node]!!)
        val faces = wallsInRange.map { world.realm.mesh.faces[it]!! }
        val walls = getWallCollisions(MovingBody(body.radius!!, body.position), offset, faces)
        walls.map { Collision(body.id, null, it.wall, it.hitPoint, it.directGap, it.travelingGap) }
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
      depictions = updateDepictions(world, animationDurations),
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
//      println("Skipped a frame.  deltaAggregator = " + deltaAggregator + " simulationDelta = " + simulationDelta)
      deltaAggregator = deltaAggregator % simulationDelta
    }
    return updateWorldMain(animationDurations, world.deck, world, commands, simulationDelta)
  } else {
    return world
  }
}