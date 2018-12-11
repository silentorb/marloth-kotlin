package simulation

import intellect.execution.pursueGoals
import intellect.updateAiState
import mythic.ent.*
import mythic.spatial.Pi2
import mythic.spatial.Vector3
import physics.Collision
import physics.Collisions
import physics.updateBodies
import physics.MovingBody
import physics.getWallCollisions
import physics.wallsInCollisionRange
import simulation.input.updatePlayer

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

fun getFinished(deck: Deck): List<Id> {
  return deck.missiles.values
      .filter { isFinished(it) }
      .map { it.id }
      .plus(deck.characters.values
          .filter { !it.isAlive && !isPlayer(deck, it) }
          .map { it.id })
}

fun removeFinished(deck: Deck, finishedIds: List<Id>): Deck {
  val isActive = { id: Id -> !finishedIds.contains(id) }

  return deck.copy(
      characters = deck.characters.filterKeys(isActive),
      depictions = deck.depictions.filterKeys(isActive),
      missiles = deck.missiles.filterKeys(isActive),
      bodies = deck.bodies.filterKeys(isActive),
      spirits = deck.spirits.filterKeys(isActive)
  )
}

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions
)

fun generateIntermediateRecords(world: World, playerCommands: Commands, delta: Float): Intermediate {
  val spiritCommands = pursueGoals(world, world.spirits)
  val commands = playerCommands.plus(spiritCommands)
  val collisions: Collisions = world.bodies
      .filter { it.velocity != Vector3.zero }
      .flatMap { body ->
        val offset = body.velocity * delta
        val wallsInRange = wallsInCollisionRange(world.realm, body.node)
        val faces = wallsInRange.map { world.realm.mesh.faces[it]!! }
        val walls = getWallCollisions(MovingBody(body.radius!!, body.position), offset, faces)
        walls.map { Collision(body.id, null, it.wall, it.hitPoint, it.directGap, it.travelingGap) }
      }
      .plus(getBodyCollisions(world.deck.bodies, world.characterTable, world.missiles))
  val activatedAbilities = getActivatedAbilities(world, commands)

  return Intermediate(
      commands = commands,
      activatedAbilities = activatedAbilities,
      collisions = collisions
  )
}

fun updateEntities(animationDurations: AnimationDurationMap, world: World, data: Intermediate): (Deck) -> Deck =
    { deck ->
      val (commands, activatedAbilities, collisionMap) = data

      val bodies = updateBodies(world.copy(deck = deck), commands, collisionMap)
      val bodyWorld = world.copy(
          deck = deck.copy(bodies = bodies)
      )

      deck.copy(
          bodies = bodies,
          depictions = mapTable(deck.depictions, updateDepiction(bodyWorld, animationDurations)),
          characters = mapTable(deck.characters, updateCharacter(bodyWorld, collisionMap, commands, activatedAbilities)),
          missiles = mapTable(deck.missiles, updateMissile(bodyWorld, collisionMap, simulationDelta)),
          players = mapTable(deck.players, updatePlayer(data.commands)),
          spirits = mapTable(deck.spirits, updateAiState(bodyWorld, simulationDelta))
      )
    }

val removeEntities: (Deck) -> Deck = { deck ->
  val finished = getFinished(deck)
  removeFinished(deck, finished)
}

fun newEntities(world: World, nextId: IdSource, data: Intermediate): (Deck) -> Deck = { deck ->
  deck.plus(getNewMissiles(world.copy(deck = deck), nextId, data.activatedAbilities))
}

fun updateWorldMain(animationDurations: AnimationDurationMap, deck: Deck, world: World, playerCommands: Commands, delta: Float): World {
  val nextId: IdSource = newIdSource(world.nextId)
  val data = generateIntermediateRecords(world, playerCommands, delta)

  val finalDeck = pipe(deck, listOf(
      updateEntities(animationDurations, world, data),
      removeEntities,
      newEntities(world, nextId, data)
  ))

//  val updatedDeck = updateEntities(animationDurations, deck, world, data)
//  val finalDeck = removeEntities(updatedDeck, world)
//      .plus(newEntities(world, nextId, data))

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