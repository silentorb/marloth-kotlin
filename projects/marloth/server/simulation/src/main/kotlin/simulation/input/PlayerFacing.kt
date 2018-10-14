package simulation.input

//fun updateCharacterFacing(child: Character, commands: Commands): Vector3 {
//  val delta = simulationDelta
//  val vector = updateTpCameraRotation(player, delta)
//  return if (vector != null)
//    child.facingRotation + vector
//  else
//    child.facingRotation
//}

//fun allFacingChanges(playerCharacters: PlayerCharacters, commands: Commands): Map<Id, Vector3> =
//    playerCharacters.mapNotNull { pc ->
//      updateCharacterFacing(pc.player, pc.child, commands.filter({ it.target == pc.player.playerId }))
//    }
//        .associate { it }
