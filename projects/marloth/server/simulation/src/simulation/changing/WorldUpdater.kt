package simulation.changing

import intellect.pursueGoals
import intellect.updateAiState
import mythic.breeze.applyAnimation
import mythic.spatial.Quaternion
import org.joml.plus
import physics.Body
import physics.applyForces
import physics.commonShapes
import physics.updateBodies
import simulation.*
import simulation.combat.*
import simulation.input.allPlayerMovements
import simulation.input.updatePlayers

fun <T> updateField(defaultValue: T, newValue: T?): T =
    if (newValue != null)
      newValue
    else
      defaultValue

fun updateCharacter(world: World, character: Character, commands: Commands, collisions: List<Collision>, delta: Float): Character {
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
  val lookForce = characterLookForce(character, commands)
  val lookVelocity = updatePlayerLookVelocity(lookForce, character.lookVelocity)

  val hits = collisions.filter { it.second == character.id }
  val health = modifyResource(character.health, hits.map { -50 })

  return character.copy(
      isAlive = character.health.value > 0,
      lookVelocity = lookVelocity,
      facingRotation = character.facingRotation + fpCameraRotation(lookVelocity, delta),
      health = character.health.copy(value = health)
  )
}

fun updateCharacters(world: World, collisions: List<Collision>, commands: Commands): List<Character> {
  val delta = simulationDelta
  return world.characterTable.map{ e ->
    val character = e.value
    val id = character.id
    updateCharacter(world, character, commands.filter { it.target == id }, collisions, delta)
  }
}

fun updateBodies(world: World, commands: Commands): List<Body> {
  val delta = simulationDelta
  val forces = allPlayerMovements(world.characterTable, commands)
  applyForces(forces, delta)
  updateBodies(world.meta, world.bodies, delta)
  return world.bodies.map {
    updateBodyNode(it)
    it
  }
}

fun getNewBodies(newMissiles: List<NewMissile>): List<Body> {
  return newMissiles.map { newMissile ->
    Body(
        id = newMissile.id,
        shape = commonShapes[EntityType.missile]!!,
        position = newMissile.position,
        orientation = Quaternion(),
        velocity = newMissile.velocity,
        node = newMissile.node,
        attributes = missileBodyAttributes
    )
  }
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
    world.characterTable = world.characterTable.minus(finished)
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
    val collisions = getCollisions(world.bodyTable, world.characterTable, world.missiles)
    val characters = updateCharacters(world, collisions, commands)
    val newMissiles = getNewMissiles(world, commands)
    val finished = getFinished()
    world.missileTable = updateMissiles(world, newMissiles, collisions, finished)
        .associate { Pair(it.id, it) }
    world.bodyTable = updateBodies(world, commands)
        .filter { body -> finished.none { it == body.id } }
        .plus(getNewBodies(newMissiles))
        .associate { Pair(it.id, it) }

    world.characterTable = characters.associate { Pair(it.id, it) }
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