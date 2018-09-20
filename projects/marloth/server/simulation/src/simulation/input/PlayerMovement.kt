package simulation.input

import mythic.spatial.Pi
import mythic.spatial.Quaternion
import org.joml.times
import physics.Body
import physics.MovementForce
import simulation.*
import simulation.changing.joinInputVector
import simulation.changing.playerMoveMap

//fun playerMovementTp(player: Player, commands: Commands, child: Character, body: Body): MovementForce? {
//  var offset = joinInputVector(commands, playerMoveMap)
//  if (offset != null) {
//    offset = (Quaternion().rotateZ(player.hoverCamera.yaw + Pi / 2) * offset)
//    setCharacterFacing(child, offset)
//    return MovementForce(body = body, offset = offset, maximum = 6f)
//  } else {
//    return null
//  }
//}
