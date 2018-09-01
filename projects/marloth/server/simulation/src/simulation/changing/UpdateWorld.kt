package simulation.changing

import intellect.pursueGoals
import intellect.updateAiState
import mythic.spatial.Pi2
import mythic.spatial.times
import physics.Collision
import physics.Collisions
import physics.updateBodies
import simulation.*
import simulation.combat.*
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

fun checkMissileBodies(missileTable: Map<Id, Missile>, bodyTable: BodyTable) {
  val incomplete = missileTable.values.filter { !bodyTable.containsKey(it.id) }
  assert(incomplete.none())
}

fun getFinished(world: World): List<Id> {
  return world.missileTable.values
      .filter { simulation.combat.isFinished(world.realm, world.bodyTable, it) }
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

//data class NewEntities(
//    val newCharacters: List<NewCharacter> = listOf(),
//    val newFurnishings: List<NewFurnishing> = listOf(),
//    val newMissiles: List<NewMissile> = listOf()
//)

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions
)

//fun getNewEntities(world: World, nextId: IdSource, data: Intermediate): Deck {
//  return Deck(
//      newCharacters = listOf(),
//      newMissiles = getNewMissiles(world, nextId, data.activatedAbilities)
//  )
//}

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

fun updateEntities(deck: Deck, world: World, data: Intermediate): Deck {
  val (commands, activatedAbilities, collisionMap) = data

  return deck.copy(
      bodies = updateBodies(world, commands, collisionMap),
      characters = updateCharacters(world, collisionMap, commands, activatedAbilities),
      missiles = updateMissiles(world, collisionMap),
      players = updatePlayers(deck.players, data.commands),
      spirits = deck.spirits.map { updateAiState(world, it) }
  )
}

//fun createNewEntitiesWorld(data: Deck): Deck {
//  val characters = getNewCharacters(data.newCharacters)
//  return Deck(
//      bodies = getNewBodies(data),
//      characters = characters,
//      furnishings = data.newFurnishings.map { Furnishing(it.id, it.type) },
//      missiles = data.newMissiles.map { Missile(it.id, it.owner, it.range) },
//      players = listOf(),
//      spirits = getNewSpirits(data.newCharacters.mapNotNull { it.spirit })
//  )
//}

fun removeEntities(deck: Deck, world: World): Deck {
  val finished = getFinished(world)
  return removeFinished(deck, finished)
}

fun updateWorldMain(deck: Deck, world: World, playerCommands: Commands, delta: Float): World {
  val nextId: IdSource = newIdSource(world.nextId)
  val data = generateIntermediateRecords(world, playerCommands, delta)
  val updatedDeck = updateEntities(deck, world, data)
  val finalDeck = removeEntities(updatedDeck, world)
  return world.copy(
      deck = finalDeck,
      nextId = nextId(),
      tables = toTables(deck)
  )
}

const val simulationDelta = 1f / 60f

private var deltaAggregator = 0f

fun updateWorld(world: World, commands: Commands, delta: Float): World {
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
    return updateWorldMain(world.deck, world, commands, simulationDelta)
  } else {
    return world
  }
}