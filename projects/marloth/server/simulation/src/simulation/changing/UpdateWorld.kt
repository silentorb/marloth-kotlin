package simulation.changing

import intellect.pursueGoals
import intellect.updateAiState
import mythic.spatial.Pi2
import mythic.spatial.Quaternion
import physics.*
import simulation.*
import simulation.combat.*
import simulation.input.allPlayerMovements
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

fun getFinished(world: World): List<Int> {
  return world.missileTable.values
      .filter { simulation.combat.isFinished(world.meta, world.bodyTable, it) }
      .map { it.id }
      .plus(world.characters
          .filter { isFinished(world, it) && !isPlayer(world, it) }
          .map { it.id })
//      .plus(world.bodies.filter { it.node == voidNode }.map { it.id })
}

fun removeFinished(world: World, finished: List<Int>) {
  world.characterTable = world.characterTable.minus(finished)
  world.missileTable = world.missileTable.minus(finished)
  world.bodyTable = world.bodyTable.minus(finished)
  world.spiritTable.minusAssign(finished)
  world.depictionTable.minusAssign(finished)
  world.lights.minusAssign(finished)
}

fun updateWorldMain(world: World, playerCommands: Commands, delta: Float) {
  val playerCharacters = world.playerCharacters
      .filter { it.character.isAlive }

  world.spiritTable = world.spiritTable.mapValues { updateAiState(world, it.value) }.toMutableMap()
  val spiritCommands = pursueGoals(world.spirits)
  val commands = playerCommands.plus(spiritCommands)
  world.players = updatePlayers(world.players, commands)
  val collisions = getCollisions(world.bodyTable, world.characterTable, world.missiles)
  val activatedAbilities = getActivatedAbilities(world, commands)
  val characters = updateCharacters(world, collisions, commands, activatedAbilities)
  val newMissiles = getNewMissiles(world, activatedAbilities)
  val finished = getFinished(world)
  checkMissileBodies(world.missileTable, world.bodyTable)
  world.missileTable = updateMissiles(world, newMissiles, collisions, finished)
      .associate { Pair(it.id, it) }
  val finishedBodies = world.bodies.filter { body -> finished.any { it == body.id } }
  if (newMissiles.any()) {
    val k = 1
  }
  world.bodyTable = updateBodies(world, commands)
      .plus(getNewBodies(newMissiles))
      .associate { Pair(it.id, it) }

  world.characterTable = characters.associate { Pair(it.id, it) }
  checkMissileBodies(world.missileTable, world.bodyTable)
  removeFinished(world, finished)

  checkMissileBodies(world.missileTable, world.bodyTable)
}

const val simulationDelta = 1f / 60f

private var deltaAggregator = 0f

fun updateWorld(world: World, commands: Commands, delta: Float) {
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
    updateWorldMain(world, commands, simulationDelta)
  }
}