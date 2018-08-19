package simulation.input

import simulation.CommandType
import mythic.spatial.Vector2
import simulation.Commands
import simulation.Player
import simulation.Players
import simulation.ViewMode
import simulation.changing.*

fun filterCommands(player: Player, commands: Commands) =
    commands.filter({ it.target == player.playerId })

private val viewModes = ViewMode.values()

fun updatePlayerView(viewMode: ViewMode, commands: Commands): ViewMode {
//  if (commands.any()) {
//    println(commands.size)
//  }
  return if (commands.any { it.type == CommandType.switchView }) {
    val index = viewModes.indexOf(viewMode)
    val nextIndex = (index + 1) % viewModes.size
    println("" + index + " " + nextIndex)
    viewModes[nextIndex]
  } else
    viewMode
}

fun updatePlayer(player: Player, commands: Commands): Player {
  val delta = simulationDelta
//  val lookForce = updateField(Vector2(), updatePlayerLookForce(player, commands))
//  val lookVelocity = updatePlayerLookVelocity(lookForce, player.lookVelocity)
//  applyPlayerLookCommands(player, commands, delta)

  return player.copy(
            viewMode = updatePlayerView(player.viewMode, commands)
  )
}

fun updatePlayers(players: Players, commands: Commands): Players {
//  if (filterCommands(players.first(), commands).size != commands.size)
//    throw Error("")
  if (commands.any { it.type == CommandType.switchView })
    println("hello")

  return players.map { updatePlayer(it, filterCommands(it, commands)) }
}