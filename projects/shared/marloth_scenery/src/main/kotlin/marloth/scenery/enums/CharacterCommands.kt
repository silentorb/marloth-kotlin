package marloth.scenery.enums

import silentorb.mythic.cameraman.CameramanCommands

object CharacterRigCommands {
  const val lookLeft = CameramanCommands.lookLeft
  const val lookRight = CameramanCommands.lookRight
  const val lookUp = CameramanCommands.lookUp
  const val lookDown = CameramanCommands.lookDown

  const val moveUp = CameramanCommands.moveUp
  const val moveDown = CameramanCommands.moveDown
  const val moveLeft = CameramanCommands.moveLeft
  const val moveRight = CameramanCommands.moveRight

  const val switchView = "switchView"
}

object CharacterCommands {
  const val lookLeft = CharacterRigCommands.lookLeft
  const val lookRight = CharacterRigCommands.lookRight
  const val lookUp = CharacterRigCommands.lookUp
  const val lookDown = CharacterRigCommands.lookDown

  const val moveUp = CharacterRigCommands.moveUp
  const val moveDown = CharacterRigCommands.moveDown
  const val moveLeft = CharacterRigCommands.moveLeft
  const val moveRight = CharacterRigCommands.moveRight

  const val equipSlot0 = "equipSlot0"
  const val equipSlot1 = "equipSlot1"
  const val equipSlot2 = "equipSlot2"
  const val equipSlot3 = "equipSlot3"

  const val interactPrimary = "interactPrimary"
  const val stopInteracting = "stopInteracting"

  const val abilityAttack = "abilityAttack"
  const val abilityDefense = "abilityDefense"
  const val abilityMobility = "abilityMobility"
  const val abilityUtility = "abilityUtility"

}
