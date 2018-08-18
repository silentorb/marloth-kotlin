package simulation.changing

import commanding.CommandType
import haft.Commands
import intellect.allSpiritMovements
import intellect.updateAiState
import mythic.breeze.applyAnimation
import mythic.spatial.Quaternion
import physics.applyForces
import physics.updateBodies
import simulation.*
import simulation.input.allPlayerFacingChanges
import simulation.input.allPlayerMovements
import simulation.input.updatePlayers

fun <T> updateField(defaultValue: T, newValue: T?): T =
    if (newValue != null)
      newValue
    else
      defaultValue

fun updateCharacters(world: World, playerCharacters: PlayerCharacters, commands: Commands<CommandType>) {
  val delta = simulationDelta
  val changes = allPlayerFacingChanges(playerCharacters, commands)

  world.characterTable = world.characterTable.mapValues { e ->
    val character = e.value
    val id = character.id
    character.copy(
        facingRotation = updateField(character.facingRotation, changes[id])
    )
  }.toMutableMap()
}

fun updatePhysics(world: World, playerCharacters: PlayerCharacters, commands: Commands<CommandType>) {
  val delta = simulationDelta
  val forces = allPlayerMovements(playerCharacters, commands).plus(allSpiritMovements(world.spirits))
  applyForces(forces, delta)
  updateBodies(world.meta, world.bodies, delta)
  world.bodies.forEach { updateBodyNode(it) }
}

class WorldUpdater(val world: World, val instantiator: Instantiator) {

  fun updateCharacter(character: Character, delta: Float) {
    if (character.isAlive) {
      character.abilities.forEach { updateAbility(it, delta) }
      character.body.orientation = Quaternion()
          .rotateZ(character.facingRotation.z)
//          .rotateX(character.rotation.x)

      val depiction = world.depictionTable[character.id]!!
      val animationInfo = depiction.animation!!
      val animation = animationInfo.armature.animations[animationInfo.animationIndex]
      animationInfo.timeOffset = (animationInfo.timeOffset + delta) % animation.duration
      applyAnimation(animation, animationInfo.armature.bones, animationInfo.timeOffset)
    } else {

    }
  }

//  fun updateCharacters(delta: Float) {
//    world.characters.forEach { updateCharacter(it, delta) }
//  }

  fun createMissiles(newMissiles: List<NewMissile>) {
    for (newMissile in newMissiles) {
      instantiator.createMissile(newMissile)
    }
  }

  fun updateDead() {
    val died = world.characters.filter { it.isAlive == true && it.health.value == 0 }
    died.forEach {
      it.isAlive = false
      it.body.shape = null
    }
  }

  fun getFinished(): List<Int> {
    return world.missileTable.values
        .filter { isFinished(world, it) }
        .map { it.id }
        .plus(world.characters
            .filter { isFinished(world, it) && !isPlayer(world, it) }
            .map { it.id })
  }

  fun removeFinished(finished: List<Int>) {
    world.missileTable.minusAssign(finished)
    world.bodyTable.minusAssign(finished)
    world.entities.minusAssign(finished)
    world.characterTable.minusAssign(finished)
    world.spiritTable.minusAssign(finished)
    world.depictionTable.minusAssign(finished)
    world.lights.minusAssign(finished)
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    val playerCharacters = world.playerCharacters
        .filter { it.character.isAlive }

    world.spiritTable = world.spiritTable.mapValues { updateAiState(world, it.value) }.toMutableMap()
//    val spiritResults = world.spirits.map { CharacterAction(world.characterTable[it.id]!!, pursueGoal(it)) }
    val playerResults = applyCommands(world, instantiator, commands, delta)
//    val results = spiritResults.plus(playerResults)
//    applyActions(world, results)
//    val forces = actionsToForces(results)
//    val newMissiles = results.mapNotNull { it.newMissile }
    world.players = updatePlayers(world.players, commands)
    updateCharacters(world, playerCharacters, commands)
    updatePhysics(world, playerCharacters, commands)
    world.missileTable.values.forEach { updateMissile(world, it, delta) }
    updateDead()
    val finished = getFinished()
    removeFinished(finished)

//    createMissiles(newMissiles)
  }
}

const val simulationDelta = 1f / 60f

private var deltaAggregator = 0f

fun updateWorld(world: World, instantiator: Instantiator, commands: Commands<CommandType>, delta: Float) {
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
    val updater = WorldUpdater(world, instantiator)
    updater.update(commands, simulationDelta)
  }
//  val updater = WorldUpdater(world, instantiator)
//  updater.update(commands, delta)
}