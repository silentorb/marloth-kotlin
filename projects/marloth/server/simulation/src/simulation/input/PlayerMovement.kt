package simulation.input

import mythic.spatial.Pi
import mythic.spatial.Quaternion
import org.joml.times
import physics.Body
import physics.MovementForce
import simulation.*
import simulation.changing.joinInputVector
import simulation.changing.playerMoveMap
import simulation.changing.setCharacterFacing

fun playerMovementTp(player: Player, commands: Commands, character: Character, body: Body): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)
  if (offset != null) {
    offset = (Quaternion().rotateZ(player.hoverCamera.yaw + Pi / 2) * offset)
    setCharacterFacing(character, offset)
    return MovementForce(body = body, offset = offset, maximum = 6f)
  } else {
    return null
  }
}

fun playerMovementFp(commands: Commands, character: Character, body: Body): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)
  if (offset != null) {
    offset = (Quaternion().rotateZ(character.facingRotation.z - Pi / 2) * offset)
    return MovementForce(body = body, offset = offset, maximum = 6f)
  } else {
    return null
  }
}

fun allPlayerMovements(world: World, commands: Commands): List<MovementForce> =
    world.characters.mapNotNull { playerMovementFp(filterCommands(it.id, commands), it, world.bodyTable[it.id]!!) }
