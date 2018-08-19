package simulation.input

import simulation.CommandType
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import org.joml.times
import physics.MovementForce
import simulation.*
import simulation.changing.joinInputVector
import simulation.changing.playerMoveMap
import simulation.changing.setCharacterFacing

fun playerMovement(player: Player, commands: Commands, character: Character): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)

  if (offset != null) {
    if (player.viewMode == ViewMode.firstPerson) {
      offset = (Quaternion().rotateZ(character.facingRotation.z - Pi / 2) * offset)
    } else if (player.viewMode == ViewMode.thirdPerson) {
      offset = (Quaternion().rotateZ(player.hoverCamera.yaw + Pi / 2) * offset)
      setCharacterFacing(character, offset)
    } else {
      setCharacterFacing(character, offset)
    }
    return MovementForce(body = character.body, offset = offset, maximum = 6f)
  } else {
    return null
  }
}

fun allPlayerMovements(playerCharacters: PlayerCharacters, commands: Commands): List<MovementForce> =
    playerCharacters.mapNotNull { pc -> playerMovement(pc.player, filterCommands(pc.player, commands), pc.character) }
