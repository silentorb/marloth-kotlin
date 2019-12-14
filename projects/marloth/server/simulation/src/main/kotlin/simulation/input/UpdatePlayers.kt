package simulation.input

import silentorb.mythic.commanding.CommonCharacterCommands
import silentorb.mythic.commanding.Commands
import silentorb.mythic.ent.Id
import simulation.updating.simulationDelta
import simulation.entities.Player
import simulation.entities.ViewMode

//private val viewModes = ViewMode.values()

//fun updatePlayerView(viewMode: ViewMode, commands: Commands): ViewMode {
////  if (commands.any()) {
////    println(commands.size)
////  }
//  return if (commands.any { it.type == CommonCharacterCommands.switchView }) {
//    val index = viewModes.indexOf(viewMode)
//    val nextIndex = (index + 1) % viewModes.size
//    println("" + index + " " + nextIndex)
//    viewModes[nextIndex]
//  } else
//    viewMode
//}
//
//fun updatePlayer(commands: Commands): (Id, Player) -> Player = { id, player ->
//  val delta = simulationDelta
////  val lookForce = updateField(Vector2(), updatePlayerLookForce(player, commands))
////  val lookVelocity = transitionVector(lookForce, player.lookVelocity)
////  applyPlayerLookCommands(player, commands, delta)
//
//  val playerCommands = filterCommands(id, commands)
//  player.copy(
//      viewMode = updatePlayerView(player.viewMode, playerCommands)
//  )
//}

//fun updatePlayers(players: Players, commands: Commands): Players {
////  if (filterCommands(players.first(), commands).size != commands.size)
////    throw Error("")
//
//  return players.map { updatePlayer(it, filterCommands(it.id, commands)) }
//}
