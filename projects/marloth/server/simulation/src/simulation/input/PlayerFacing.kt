package simulation.input

import mythic.spatial.Vector3
import org.joml.plus
import simulation.*
import simulation.changing.applyPlayerLookCommands
import simulation.changing.characterLookForce
import simulation.changing.simulationDelta
import simulation.changing.updatePlayerRotation

fun updateCharacterFacing(character: Character, commands: Commands): Vector3 {
  val delta = simulationDelta
  val lookForce = characterLookForce(commands)
  val vector = updatePlayerRotation(player, delta)
  return if (vector != null)
    character.facingRotation + vector
  else
    character.facingRotation
}

//fun allFacingChanges(playerCharacters: PlayerCharacters, commands: Commands): Map<Id, Vector3> =
//    playerCharacters.mapNotNull { pc ->
//      updateCharacterFacing(pc.player, pc.character, commands.filter({ it.target == pc.player.playerId }))
//    }
//        .associate { it }
