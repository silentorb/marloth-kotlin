package simulation.input

//fun updateCharacterFacing(character: Character, commands: Commands): Vector3 {
//  val delta = simulationDelta
//  val vector = updateTpCameraRotation(player, delta)
//  return if (vector != null)
//    character.facingRotation + vector
//  else
//    character.facingRotation
//}

//fun allFacingChanges(playerCharacters: PlayerCharacters, commands: Commands): Map<Id, Vector3> =
//    playerCharacters.mapNotNull { pc ->
//      updateCharacterFacing(pc.player, pc.character, commands.filter({ it.target == pc.player.playerId }))
//    }
//        .associate { it }
