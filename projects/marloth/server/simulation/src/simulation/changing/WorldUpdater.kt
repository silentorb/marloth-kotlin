package simulation.changing

import intellect.pursueGoals
import intellect.updateAiState
import mythic.breeze.applyAnimation
import mythic.spatial.Quaternion
import org.joml.plus
import physics.applyForces
import physics.updateBodies
import simulation.*
import simulation.combat.updateMissiles
import simulation.input.allPlayerMovements
import simulation.input.updatePlayers

fun <T> updateField(defaultValue: T, newValue: T?): T =
    if (newValue != null)
      newValue
    else
      defaultValue

fun updateCharacter(world: World, character: Character, commands: Commands, delta: Float): Character {
  val id = character.id
  if (character.isAlive) {
    character.abilities.forEach { updateAbility(it, delta) }
    character.body.orientation = Quaternion()
        .rotateZ(character.facingRotation.z)

    val depiction = world.depictionTable[character.id]!!
    val animationInfo = depiction.animation!!
    val animation = animationInfo.armature.animations[animationInfo.animationIndex]
    animationInfo.timeOffset = (animationInfo.timeOffset + delta) % animation.duration
    applyAnimation(animation, animationInfo.armature.bones, animationInfo.timeOffset)
  } else {

  }
  val lookForce = characterLookForce(commands)
  val lookVelocity = updatePlayerLookVelocity(lookForce, character.lookVelocity)

  return character.copy(
      isAlive = character.health.value > 0,
      lookVelocity = lookVelocity,
      facingRotation = character.facingRotation + fpCameraRotation(lookVelocity, delta)
  )
}

fun updateCharacters(world: World, commands: Commands) {
  val delta = simulationDelta
  world.characterTable = world.characterTable.mapValues { e ->
    val character = e.value
    val id = character.id
    updateCharacter(world, character, commands.filter { it.target == id }, delta)
  }.toMutableMap()
}

fun updateBodies(world: World, commands: Commands) {
  val delta = simulationDelta
  val forces = allPlayerMovements(world.characterTable, commands)
  applyForces(forces, delta)
  updateBodies(world.meta, world.bodies, delta)
  world.bodies.forEach { updateBodyNode(it) }
}

class WorldUpdater(val world: World) {

  fun getFinished(): List<Int> {
    return world.missileTable.values
        .filter { simulation.combat.isFinished(world.meta, world.bodyTable, it) }
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

  fun update(playerCommands: Commands, delta: Float) {
    val playerCharacters = world.playerCharacters
        .filter { it.character.isAlive }

    world.spiritTable = world.spiritTable.mapValues { updateAiState(world, it.value) }.toMutableMap()
    val spiritCommands = pursueGoals(world.spirits)
    val commands = playerCommands.plus(spiritCommands)
    world.players = updatePlayers(world.players, commands)
    updateCharacters(world, commands)
    updateMissiles(world, commands)
    updateBodies(world, commands)
    val finished = getFinished()
    removeFinished(finished)
  }
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
    val updater = WorldUpdater(world)
    updater.update(commands, simulationDelta)
  }
//  val updater = WorldUpdater(world, instantiator)
//  updater.update(commands, delta)
}