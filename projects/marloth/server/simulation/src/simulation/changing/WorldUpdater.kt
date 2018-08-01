package simulation.changing

import commanding.CommandType
import haft.Commands
import intellect.CharacterResult
import intellect.updateSpirit
import mythic.breeze.applyAnimation
import mythic.spatial.Quaternion
import physics.applyForces
import physics.updateBodies
import simulation.*

private val viewModes = ViewMode.values()

class WorldUpdater(val world: World, val instantiator: Instantiator) {

  fun switchCameraMode(player: Player) {
    val currentMode = player.viewMode
    val index = viewModes.indexOf(currentMode)
    val nextIndex = (index + 1) % viewModes.size
    player.viewMode = viewModes[nextIndex]
  }

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float): CharacterResult {
    if (commands.isEmpty())
      return CharacterResult()

    val force = playerMove(player, commands)

    for (command in commands) {
      when (command.type) {
        CommandType.switchView -> switchCameraMode(player)
      }
    }

    applyPlayerLookCommands(player, commands, delta)

    return CharacterResult(
        newMissile = playerAttack(player, commands),
        forces = if (force != null) listOf(force) else listOf()
    )
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float): List<CharacterResult> {
    val playerResults = players
        .filter { it.character.isAlive }
        .map { player ->
          val result = applyPlayerCommands(player, commands.filter({ it.target == player.playerId }), delta)
          updatePlayerRotation(player, delta)
          result
        }

    val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
    for (command in remainingCommands) {
      if (command.type == CommandType.joinGame) {
        instantiator.createPlayer(world.players.size + 1)
      }
    }

    return playerResults
  }

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
    val spiritResults = world.spirits.map { updateSpirit(world, it, delta) }
    val playerResults = applyCommands(world.players, commands, delta)
    val results = spiritResults.plus(playerResults)
    val newMissiles = results.mapNotNull { it.newMissile }

    applyForces(results.flatMap { it.forces }, delta)
    updateBodies(world.meta, world.bodies, delta)
    world.missileTable.values.forEach { updateMissile(world, it, delta) }
    world.bodies.forEach { updateBodyNode(it) }
    updateDead()
    val finished = getFinished()
    removeFinished(finished)

    createMissiles(newMissiles)
  }
}
