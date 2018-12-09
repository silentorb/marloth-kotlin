package simulation.input

import mythic.ent.Id
import simulation.*

fun filterCommands(id: Id, commands: Commands) =
    commands.filter({ it.target == id })

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
//  val lookVelocity = transitionVector(lookForce, player.lookVelocity)
//  applyPlayerLookCommands(player, commands, delta)

  return player.copy(
      viewMode = updatePlayerView(player.viewMode, commands)
  )
}

fun updatePlayers(players: Players, commands: Commands): Players {
//  if (filterCommands(players.first(), commands).size != commands.size)
//    throw Error("")

  return players.map { updatePlayer(it, filterCommands(it.id, commands)) }
}