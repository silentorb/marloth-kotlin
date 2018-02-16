package simulation

import commanding.CommandType
import haft.Commands
import intellect.getAiCharacters
import intellect.updateAi

class WorldUpdater(val world: World) {

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float): NewMissile? {
    if (commands.isEmpty())
      return null

    playerMove(world, player.character.body, commands, delta)
    return playerAttack(world, player.character, commands)
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float): List<NewMissile> {
    val result = players.mapNotNull { player ->
      applyPlayerCommands(player, commands.filter({ it.target == player.playerId }), delta)
    }

    val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
    for (command in remainingCommands) {
      if (command.type == CommandType.joinGame) {
        world.createPlayer(world.players.size + 1)
      }
    }

    return result
  }

  fun updateCharacter(character: Character, delta: Float) {
    character.abilities.forEach { updateAbility(it, delta) }
  }

  fun updateCharacters(delta: Float) {
    world.characters.forEach { updateCharacter(it, delta) }
  }

  fun createMissiles(newMissiles: List<NewMissile>) {
    for (newMissile in newMissiles) {
      world.createMissile(newMissile)
    }
  }

  fun getFinished(): List<Int> {
    return world.missileTable.values
        .filter { isFinished(world, it) }
        .map { it.id }
        .plus(world.characters
            .filter { isFinished(world, it) }
            .map { it.id })
  }

  fun removeFinished(finished: List<Int>) {
    world.missileTable.minusAssign(finished)
    world.bodyTable.minusAssign(finished)
    world.entities.minusAssign(finished)
    world.characterTable.minusAssign(finished)
    world.spiritTable.minusAssign(finished)
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    updateCharacters(delta)
    val aiCharacters = getAiCharacters(world)
    val newMissiles = world.spirits.mapNotNull { updateAi(world, it) }
        .plus(applyCommands(world.players, commands, delta))

    world.missileTable.values.forEach { updateMissile(world, it, delta) }

    val finished = getFinished()
    removeFinished(finished)

    createMissiles(newMissiles)
  }
}
