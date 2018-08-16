package simulation.changing

import commanding.CommandType
import haft.Commands
import intellect.pursueGoal
import intellect.updateAiState
import mythic.breeze.applyAnimation
import mythic.spatial.Quaternion
import physics.applyForces
import physics.updateBodies
import simulation.*

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

  fun updateCharacters(delta: Float) {
    world.characters.forEach { updateCharacter(it, delta) }
  }

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
    updateCharacters(delta)
    world.spiritTable = world.spiritTable.mapValues { updateAiState(world, it.value) }.toMutableMap()
    val spiritResults = world.spirits.map { CharacterAction(world.characterTable[it.id]!!, pursueGoal(it)) }
    val playerResults = applyCommands(world, instantiator, commands, delta)
    val results = spiritResults.plus(playerResults)
    applyActions(world, results)
    val forces = actionsToForces(world, results)
//    val newMissiles = results.mapNotNull { it.newMissile }

    applyForces(forces, delta)
    updateBodies(world.meta, world.bodies, delta)
    world.missileTable.values.forEach { updateMissile(world, it, delta) }
    world.bodies.forEach { updateBodyNode(it) }
    updateDead()
    val finished = getFinished()
    removeFinished(finished)

//    createMissiles(newMissiles)
  }
}

const val simulationHertz = 1f / 60f

private var deltaAggregator = 0f

fun updateWorld(world: World, instantiator: Instantiator, commands: Commands<CommandType>, delta: Float) {
  deltaAggregator += delta
  if (deltaAggregator > simulationHertz) {
    deltaAggregator -= simulationHertz

    // The above subtraction, this condition, and the modulus could all be handled by a single modulus
    // but if deltaAggregator is more than twice the simulationHertz then the simulation is not keeping
    // up and I want that to be handled as a special case, not incidentally handled by a modulus.
    if (deltaAggregator > simulationHertz) {
      deltaAggregator = deltaAggregator % simulationHertz
    }
    val updater = WorldUpdater(world, instantiator)
    updater.update(commands, simulationHertz)
  }
//  val updater = WorldUpdater(world, instantiator)
//  updater.update(commands, delta)
}