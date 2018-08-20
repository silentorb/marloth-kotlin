package simulation.input

import mythic.spatial.Pi
import mythic.spatial.Quaternion
import org.joml.times
import physics.MovementForce
import simulation.*
import simulation.changing.joinInputVector
import simulation.changing.playerMoveMap
import simulation.changing.setCharacterFacing

fun playerMovementTp(player: Player, commands: Commands, character: Character): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)
  if (offset != null) {
    offset = (Quaternion().rotateZ(player.hoverCamera.yaw + Pi / 2) * offset)
    setCharacterFacing(character, offset)
    return MovementForce(body = character.body, offset = offset, maximum = 6f)
  } else {
    return null
  }
}

fun playerMovementFp(commands: Commands, character: Character): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)
  if (offset != null) {
    offset = (Quaternion().rotateZ(character.facingRotation.z - Pi / 2) * offset)
    return MovementForce(body = character.body, offset = offset, maximum = 6f)
  } else {
    return null
  }
}

fun allPlayerMovements(characters: CharacterTable, commands: Commands): List<MovementForce> =
    characters.mapNotNull { playerMovementFp(filterCommands(it.value.id, commands), it.value) }
