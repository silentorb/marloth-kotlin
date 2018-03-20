package simulation.changing

import commanding.CommandType
import haft.Commands
import intellect.getAiCharacters
import intellect.updateAi
import mythic.spatial.Quaternion
import simulation.*

class WorldUpdater(val world: World, val instantiator: Instantiator) {

  fun switchCameraMode(player: Player) {
    val currentMode = player.viewMode
    player.viewMode =
        if (currentMode == ViewMode.topDown)
          ViewMode.firstPerson
        else
          ViewMode.topDown
  }

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float): NewMissile? {
    if (commands.isEmpty())
      return null

    playerMove(world, player, commands, delta)

    for (command in commands) {
      when (command.type) {
        CommandType.switchView -> switchCameraMode(player)
      }
    }

    if (player.viewMode == ViewMode.firstPerson) {
      playerRotate(player, commands, delta)
    }

    return playerAttack(player, commands)
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float): List<NewMissile> {
    val result = players
        .filter { it.character.isAlive }
        .mapNotNull { player ->
          applyPlayerCommands(player, commands.filter({ it.target == player.playerId }), delta)
        }

    val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
    for (command in remainingCommands) {
      if (command.type == CommandType.joinGame) {
        instantiator.createPlayer(world.players.size + 1)
      }
    }

    return result
  }

  fun updateCharacter(character: Character, delta: Float) {
    if (character.isAlive) {
      character.abilities.forEach { updateAbility(it, delta) }
      character.body.orientation = Quaternion()
          .rotateZ(character.facingRotation.z)
//          .rotateX(character.rotation.x)
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
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    updateCharacters(delta)
    val aiCharacters = getAiCharacters(world)
    val newMissiles = world.spirits.mapNotNull { updateAi(world, it) }
        .plus(applyCommands(world.players, commands, delta))

    world.missileTable.values.forEach { updateMissile(world, it, delta) }
    updateDead()
    val finished = getFinished()
    removeFinished(finished)

    createMissiles(newMissiles)
  }
}
