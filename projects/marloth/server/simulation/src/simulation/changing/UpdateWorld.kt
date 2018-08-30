package simulation.changing

import intellect.getNewSpirits
import intellect.pursueGoals
import intellect.updateAiState
import mythic.spatial.Pi2
import mythic.spatial.times
import physics.Collision
import physics.Collisions
import physics.getNewBodies
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

fun getFinished(world: WorldMap): List<Id> {
  return world.missileTable.values
      .filter { simulation.combat.isFinished(world.meta, world.bodyTable, it) }
      .map { it.id }
      .plus(world.characters
          .filter { isFinished(world, it) && !isPlayer(world, it) }
          .map { it.id })
//      .plus(world.bodies.filter { it.node == voidNode }.map { it.id })
}

fun removeFinished(world: World, finishedIds: List<Id>): World {
  val isFinished = { entity: EntityLike -> finishedIds.contains(entity.id) }

  return world.copy(
      characters = world.characters.filter(isFinished),
      missiles = world.missiles.filter(isFinished),
      bodies = world.bodies.filter(isFinished),
      spirits = world.spirits.filter(isFinished)
  )
}

data class NewEntities(
    val newCharacters: List<NewCharacter> = listOf(),
    val newMissiles: List<NewMissile> = listOf()
)

data class Intermediate(
    val commands: Commands,
    val activatedAbilities: List<ActivatedAbility>,
    val collisions: Collisions
)

fun getNewEntities(world: WorldMap, nextId: IdSource, data: Intermediate): NewEntities {
  return NewEntities(
      newCharacters = listOf(),
      newMissiles = getNewMissiles(world, nextId, data.activatedAbilities)
  )
}

fun generateIntermediateRecords(world: WorldMap, playerCommands: Commands, delta: Float): Intermediate {
  val spiritCommands = pursueGoals(world.spirits)
  val commands = playerCommands.plus(spiritCommands)
  val collisions: Collisions = world.bodies.flatMap { body ->
    val offset = body.velocity * delta
    val walls = findCollisionWalls(body.position, offset, world.meta, body.node)
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

fun updateEntities(world: World, worldMap: WorldMap, data: Intermediate): World {
  val (commands, activatedAbilities, collisionMap) = data

  return world.copy(
      bodies = updateBodies(worldMap, commands, collisionMap),
      characters = updateCharacters(worldMap, collisionMap, commands, activatedAbilities),
      missiles = updateMissiles(worldMap, collisionMap),
      players = updatePlayers(world.players, data.commands),
      spirits = world.spirits.map { updateAiState(worldMap, it) }
  )
}

fun createNewEntitiesWorld(data: NewEntities): NewEntitiesWorld {
  val characters = getNewCharacters(data.newCharacters)
  return NewEntitiesWorld(
      bodies = getNewBodies(data),
      characters = characters,
      missiles = data.newMissiles.map { Missile(it.id, it.owner, it.range) },
      players = listOf(),
      spirits = getNewSpirits(data.newCharacters.mapNotNull { it.spirit })
  )
}

fun removeEntities(world: World, worldMap: WorldMap): World {
  val finished = getFinished(worldMap)
  return removeFinished(world, finished)
}

fun addEntities(world: World, newEntities: NewEntities, nextId: IdSource): World =
    world.plus(createNewEntitiesWorld(newEntities))
        .copy(
            nextId = nextId()
        )

fun updateWorldMain(world: World, worldMap: WorldMap, playerCommands: Commands, delta: Float): World {
  val nextId: IdSource = newIdSource(world.nextId)
  val data = generateIntermediateRecords(worldMap, playerCommands, delta)
  val updatedWorld = updateEntities(world, worldMap, data)
  val newEntities = getNewEntities(worldMap, nextId, data)
  return addEntities(removeEntities(updatedWorld, worldMap), newEntities, nextId)
}

const val simulationDelta = 1f / 60f

private var deltaAggregator = 0f

fun updateWorld(world: WorldMap, commands: Commands, delta: Float): WorldMap {
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
    val newWorld = updateWorldMain(world.state, world, commands, simulationDelta)
    return generateWorldMap(newWorld)
  } else {
    return world
  }
}