package simulation.input

import commanding.CommandType
import haft.Commands
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.plus
import simulation.*
import simulation.changing.applyPlayerLookCommands
import simulation.changing.simulationDelta
import simulation.changing.updatePlayerRotation

fun playerFacingChange(player: Player, character: Character, commands: Commands<CommandType>): Pair<Id, Vector3>? {
  val delta = simulationDelta
  applyPlayerLookCommands(player, commands, delta)
  val vector = updatePlayerRotation(player, delta)
  return if (vector != null)
    Pair(player.character, character.facingRotation + vector)
  else
    null
}

fun allPlayerFacingChanges(playerCharacters: PlayerCharacters, commands: Commands<CommandType>): Map<Id, Vector3> =
    playerCharacters.mapNotNull { pc ->
      playerFacingChange(pc.player, pc.character, commands.filter({ it.target == pc.player.playerId }))
    }
        .associate { it }
