package simulation.changing

import commanding.CommandType
import haft.Commands
import intellect.CharacterResult
import mythic.spatial.Vector2
import simulation.*

private val viewModes = ViewMode.values()

fun switchCameraMode(player: Player) {
  val currentMode = player.viewMode
  val index = viewModes.indexOf(currentMode)
  val nextIndex = (index + 1) % viewModes.size
  player.viewMode = viewModes[nextIndex]
}

fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float): Actions {
  if (commands.isEmpty())
    return listOf()

  val actions = playerMove(player, commands)

  for (command in commands) {
    when (command.type) {
      CommandType.switchView -> switchCameraMode(player)
    }
  }

  applyPlayerLookCommands(player, commands, delta)

//  return CharacterResult(
//      newMissile = playerAttack(player, commands),
//      actions = if (force != null) listOf(force) else listOf()
//  )
  return actions
}

fun applyCommands(world: World, instantiator: Instantiator, commands: Commands<CommandType>, delta: Float): CharacterActions {
  val playerResults = world.players
      .filter { it.character.isAlive }
      .map { player ->
        player.lookForce = Vector2()
        val result = applyPlayerCommands(player, commands.filter({ it.target == player.playerId }), delta)
        updatePlayerRotation(player, delta)
        CharacterAction(player.character, result)
      }

  val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
  for (command in remainingCommands) {
    if (command.type == CommandType.joinGame) {
      instantiator.createPlayer(world.players.size + 1)
    }
  }

  return playerResults
}
