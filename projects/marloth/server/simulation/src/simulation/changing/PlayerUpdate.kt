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

fun applyCommands(world: World, instantiator: Instantiator, commands: Commands<CommandType>, delta: Float): List<CharacterResult> {
  val playerResults = world.players
      .filter { it.character.isAlive }
      .map { player ->
        player.lookForce = Vector2()
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
